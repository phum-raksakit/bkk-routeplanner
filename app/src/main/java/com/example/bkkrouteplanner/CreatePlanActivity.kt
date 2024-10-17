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
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CreatePlanActivity : AppCompatActivity() {

    companion object {
        private const val MAPS_ACTIVITY_REQUEST_CODE_START = 1 // Request code for MapsActivity
        private const val MAPS_ACTIVITY_REQUEST_CODE_DEST = 2
    }

    private var startPlace: String? = null // Variable to store the start place name
    private var destinationPlace: String? = null // Variable to store the current destination place
    private lateinit var destinationAdapter: ArrayAdapter<String>
    private val destinationList: MutableList<String> = mutableListOf() // List to hold all destinations


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_plan)


        setupBackButton()       // Set up the back button for navigation
        setupStartButton()      // Set up EditText button to launch MapsActivity
        setupDestinationButton()// Set up EditText button to launch MapsActivity
        setupDatePicker()       // Set up date picker dialog
        setupTimePicker()       // Set up time picker dialog
        setupCreateButton()

        // Initialize ListView and Adapter
        destinationAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, destinationList)
        val destinationListView = findViewById<ListView>(R.id.destination_listView)
        destinationListView.adapter = destinationAdapter

        // Set up the Add button to add destinations to ListView
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showNotification("New Plan Created", "Your travel plan has been saved successfully!")
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    // Handle the result returned from MapsActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                MAPS_ACTIVITY_REQUEST_CODE_START -> {
                    startPlace = data?.getStringExtra("START_PLACE")
                    if (startPlace != null) {
                        useStartPlace()
                    }
                }
                MAPS_ACTIVITY_REQUEST_CODE_DEST -> {
                    destinationPlace = data?.getStringExtra("DESTINATION_PLACE")
                    if (destinationPlace != null) {
                        useDestinationPlace()
                    }
                }
            }
        }
    }

    private fun setupDestinationButton() {
        val editTextButtonDestLocation = findViewById<EditText>(R.id.editTextDestLocation)
        editTextButtonDestLocation.setOnClickListener {
            val intent = Intent(this, MapsDestinationActivity::class.java)
            startActivityForResult(intent, MAPS_ACTIVITY_REQUEST_CODE_DEST) // Launch MapsActivity for destination
        }
    }

    // Set up EditText button to open MapsActivity
    private fun setupStartButton() {
        val editTextButtonStartDestination = findViewById<EditText>(R.id.editTextStartLocation)
        editTextButtonStartDestination.setOnClickListener {
            val intent = Intent(this, MapsStartActivity::class.java)
            startActivityForResult(intent, MAPS_ACTIVITY_REQUEST_CODE_START) // Launch MapsActivity
        }
    }

    // Set up the back button to navigate to HomepageActivity
    private fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, HomepageActivity::class.java)
            startActivity(intent) // Navigate to HomepageActivity
        }
    }

    // Set up the date picker dialog
    private fun setupDatePicker() {
        val dateEditText = findViewById<EditText>(R.id.editTextDate)
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a Date")
            .build()

        dateEditText.setOnClickListener {
            datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER") // Show date picker
        }

        datePicker.addOnPositiveButtonClickListener { selection ->
            dateEditText.setText(datePicker.headerText) // Set selected date to EditText
        }
    }

    // Set up the time picker dialog
    private fun setupTimePicker() {
        val timeEditText = findViewById<EditText>(R.id.editTextTime)
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Appointment time")
            .build()

        timeEditText.setOnClickListener {
            timePicker.show(supportFragmentManager, "MATERIAL_TIME_PICKER") // Show time picker
        }

        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute
            timeEditText.setText(String.format("%02d:%02d", selectedHour, selectedMinute)) // Set selected time
        }
    }

    // Update the start location EditText with the selected start place
    private fun useStartPlace() {
        if (startPlace != null) {
            val editTextStartLocation = findViewById<EditText>(R.id.editTextStartLocation)
            editTextStartLocation.setText(startPlace) // Set start place in EditText
        }
    }

    private fun useDestinationPlace() {
        if (destinationPlace != null) {
            val editTextDestLocation = findViewById<EditText>(R.id.editTextDestLocation)
            editTextDestLocation.setText(destinationPlace) // Set destination place in EditText
        }
    }

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

            // ตรวจสอบว่า EditText ทั้งหมดต้องไม่ว่าง
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

            // ตรวจสอบว่า startPlace ไม่เป็น null และไม่ว่าง
            if (startPlace.isNullOrEmpty()) {
                start.error = "Please select a Location"
                return@setOnClickListener
            }

            // ตรวจสอบว่า destinationList ไม่ว่างหรือเปล่า
            if (destinationList.isNullOrEmpty()) {
                destination.error = "Please add at least one destination"
                return@setOnClickListener
            }

            val planId = "Plan_${System.currentTimeMillis()}"

            val newPlan = Plan(
                id = planId,
                planName = planName,
                start = startPlace!!,  // startPlace ไม่เป็น null เพราะเราได้ตรวจสอบแล้ว
                destination = destinationList,
                date = date,
                time = time
            )

            // บันทึก Plan ลงใน SharedPreferences
            savePlanToLocalStorage(newPlan)

            Toast.makeText(this, "New Plan Created: ${newPlan.planName}", Toast.LENGTH_SHORT).show()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                } else {
                    showNotification("New Plan Created", "Your travel plan has been saved successfully! ${planId}")
                }
            } else {
                showNotification("New Plan Created", "Your travel plan has been saved successfully!")
            }
        }
    }


    private fun savePlanToLocalStorage(plan: Plan) {
        val sharedPreferences = getSharedPreferences("myPlanStorage", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()


        // แปลง Plan เป็น JSON
        val planJson = gson.toJson(plan)

        // ดึงข้อมูลแผนทั้งหมดในรูปแบบ List<Plan> จาก SharedPreferences
        val planListJson = sharedPreferences.getString("planList", "[]")
        val type = object : TypeToken<MutableList<Plan>>() {}.type
        val planList: MutableList<Plan> = gson.fromJson(planListJson, type) ?: mutableListOf()

        // เพิ่มแผนใหม่ลงในลิสต์
        planList.add(plan)

        // บันทึกลิสต์แผนใหม่กลับไปใน SharedPreferences
        editor.putString("planList", gson.toJson(planList))
        editor.apply()
    }

    private fun showNotification(title: String, text: String) {
        val channelId = "plan_channel_id"

        // ตรวจสอบและสร้าง Notification Channel สำหรับ Android 8.0 ขึ้นไป
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Plan Notifications"
            val channelDescription = "Notifications for new plans"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // สร้าง Notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_map)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // ตรวจสอบ permission ก่อนแสดง Notification
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val manager = NotificationManagerCompat.from(this)
            manager.notify(0, notification)
        } else {
            // ขอสิทธิ์ POST_NOTIFICATIONS หากยังไม่ได้รับ
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }


}
