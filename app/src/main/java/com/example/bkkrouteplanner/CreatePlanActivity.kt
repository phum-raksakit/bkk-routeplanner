package com.example.bkkrouteplanner

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CreatePlanActivity : AppCompatActivity() {

    companion object {
        private const val MAPS_ACTIVITY_REQUEST_CODE_START = 1 // Request code for MapsActivity
        private const val MAPS_ACTIVITY_REQUEST_CODE_DEST = 2 // Request code for destination selection
    }

    private var startPlace: String? = null // Variable to store the start place name
    private var destinationPlace: String? = null // Variable to store the current destination place
    private lateinit var destinationAdapter: ArrayAdapter<String>
    private val destinationList: MutableList<String> = mutableListOf() // List to hold all destinations

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_plan)

        setupBackButton()          // Initialize back button
        setupStartButton()         // Initialize start place button
        setupDestinationButton()   // Initialize destination button
        setupDatePicker()          // Initialize date picker dialog
        setupTimePicker()          // Initialize time picker dialog
        setupCreateButton()        // Initialize create plan button

        // Initialize ListView and Adapter
        destinationAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, destinationList)
        val destinationListView = findViewById<ListView>(R.id.destination_listView)
        destinationListView.adapter = destinationAdapter

        // Set up Add button for adding destinations to ListView
        val addButton = findViewById<Button>(R.id.add_button)
        addButton.setOnClickListener {
            if (!destinationPlace.isNullOrEmpty()) {
                destinationList.add(destinationPlace!!)
                destinationAdapter.notifyDataSetChanged() // Update ListView
                destinationPlace = null
                findViewById<EditText>(R.id.editTextDestLocation).text.clear() // Clear the EditText
            }
        }

    }

    // Handle notification permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showNotification(this)
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle result returned from MapsActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                MAPS_ACTIVITY_REQUEST_CODE_START -> {
                    startPlace = data?.getStringExtra("START_PLACE")
                    useStartPlace()
                }
                MAPS_ACTIVITY_REQUEST_CODE_DEST -> {
                    destinationPlace = data?.getStringExtra("DESTINATION_PLACE")
                    useDestinationPlace()
                }
            }
        }
    }

    // Open MapsActivity for selecting destination
    private fun setupDestinationButton() {
        val editTextButtonDestLocation = findViewById<EditText>(R.id.editTextDestLocation)
        editTextButtonDestLocation.setOnClickListener {
            val intent = Intent(this, MapsDestinationActivity::class.java)
            startActivityForResult(intent, MAPS_ACTIVITY_REQUEST_CODE_DEST)
        }
    }

    // Open MapsActivity for selecting start place
    private fun setupStartButton() {
        val editTextButtonStartLocation = findViewById<EditText>(R.id.editTextStartLocation)
        editTextButtonStartLocation.setOnClickListener {
            val intent = Intent(this, MapsStartActivity::class.java)
            startActivityForResult(intent, MAPS_ACTIVITY_REQUEST_CODE_START)
        }
    }

    // Setup back button to return to HomepageActivity
    private fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, HomepageActivity::class.java)
            startActivity(intent)
        }
    }

    // Setup date picker
    private fun setupDatePicker() {
        val dateEditText = findViewById<EditText>(R.id.editTextDate)
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a Date")
            .build()

        dateEditText.setOnClickListener {
            datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        }

        datePicker.addOnPositiveButtonClickListener { selection ->
            dateEditText.setText(datePicker.headerText)
        }
    }

    // Setup time picker
    private fun setupTimePicker() {
        val timeEditText = findViewById<EditText>(R.id.editTextTime)
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Appointment time")
            .build()

        timeEditText.setOnClickListener {
            timePicker.show(supportFragmentManager, "MATERIAL_TIME_PICKER")
        }

        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute
            timeEditText.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
        }
    }

    // Use the selected start place and set it in the EditText
    private fun useStartPlace() {
        if (!startPlace.isNullOrEmpty()) {
            val editTextStartLocation = findViewById<EditText>(R.id.editTextStartLocation)
            editTextStartLocation.setText(startPlace)
        }
    }

    // Use the selected destination place and set it in the EditText
    private fun useDestinationPlace() {
        if (!destinationPlace.isNullOrEmpty()) {
            val editTextDestLocation = findViewById<EditText>(R.id.editTextDestLocation)
            editTextDestLocation.setText(destinationPlace)
        }
    }

    // Setup button to create a new plan
    private fun setupCreateButton() {
        val createButton = findViewById<Button>(R.id.create_button)
        val planNameEditText = findViewById<EditText>(R.id.editTextPlanName)
        val dateEditText = findViewById<EditText>(R.id.editTextDate)
        val timeEditText = findViewById<EditText>(R.id.editTextTime)
        val start = findViewById<EditText>(R.id.editTextStartLocation)
        val destination = findViewById<EditText>(R.id.editTextDestLocation)

        createButton.setOnClickListener {
            val planName = planNameEditText.text.toString().trim()
            val date = dateEditText.text.toString().trim()
            val time = timeEditText.text.toString().trim()

            // Validate that no fields are empty
            if (planName.isEmpty()) {
                planNameEditText.error = "Please enter the plan name"
                return@setOnClickListener
            }
            if (date.isEmpty()) {
                dateEditText.error = "Please select a date"
                return@setOnClickListener
            }
            if (time.isEmpty()) {
                timeEditText.error = "Please select a time"
                return@setOnClickListener
            }
            if (startPlace.isNullOrEmpty()) {
                start.error = "Please select a location"
                return@setOnClickListener
            }
            if (destinationList.isNullOrEmpty()) {
                destination.error = "Please add at least one destination"
                return@setOnClickListener
            }

            // Create new plan with unique ID
            val planId = "Plan_${System.currentTimeMillis()}"
            val newPlan = Plan(planId, planName, startPlace!!, destinationList, date, time)

            // Save plan to local storage
            savePlanToLocalStorage(newPlan)

            Toast.makeText(this, "New Plan Created: ${newPlan.planName}", Toast.LENGTH_SHORT).show()

            // Handle notification permission for Android 13+ (Tiramisu)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                } else {
                    showNotification(this)
                }
            } else {
                showNotification(this)
            }

            // Navigate to PlanDetailsActivity
            val intent = Intent(this, PlanDetailsActivity::class.java)
            intent.putExtra("PLAN_ID", planId)
            startActivity(intent)
        }
    }

    // Save plan to SharedPreferences
    private fun savePlanToLocalStorage(plan: Plan) {
        val sharedPreferences = getSharedPreferences("PlanStorage", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        // Convert plan to JSON
        val planJson = gson.toJson(plan)

        // Retrieve and update plan list from SharedPreferences
        val planListJson = sharedPreferences.getString("planList", "[]")
        val type = object : TypeToken<MutableList<Plan>>() {}.type
        val planList: MutableList<Plan> = gson.fromJson(planListJson, type) ?: mutableListOf()

        planList.add(plan)
        editor.putString("planList", gson.toJson(planList))
        editor.apply()
    }

    // Show notification when plan is created
    private fun showNotification(context: Context) {
        val channelId = "countdown_channel"
        val channelName = "Countdown Notifications"

        // Create Notification Channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notification channel for countdown timer"
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Intent for Notification
        val notificationIntent = Intent(context, CreatePlanActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Congratulation!")
            .setContentText("Your travel plan has been saved successfully!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Display notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}
