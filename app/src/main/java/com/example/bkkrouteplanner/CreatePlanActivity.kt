package com.example.bkkrouteplanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class CreatePlanActivity : AppCompatActivity() {

    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE_START = 1
        private const val AUTOCOMPLETE_REQUEST_CODE_DESTINATION = 2
    }

    private val destinationList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_plan)

        // Set up back button to navigate to HomepageActivity
        setupBackButton()

        setupCreateButton()

        // Initialize Google Places API
        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)

        // Set up autocomplete fields for start and destination locations
        setupLocationAutocomplete()

        // Set up destination list and add button functionality
        setupDestinationList()

        // Set up date and time pickers
        setupDatePicker()
        setupTimePicker()
    }

    // Setup Back button functionality
    private fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, HomepageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupCreateButton() {
        val createButton = findViewById<Button>(R.id.create_button)
        createButton.setOnClickListener {
            val intent = Intent(this, PlanDetailsActivity::class.java)
            startActivity(intent)
        }
    }

    // Set up EditText for start and destination locations with Google Places Autocomplete
    private fun setupLocationAutocomplete() {
        val startLocationEditText = findViewById<EditText>(R.id.editTextStartLocation)
        val destinationLocationEditText = findViewById<EditText>(R.id.editTextDestLocation)

        startLocationEditText.setOnClickListener {
            startAutocompleteActivity(AUTOCOMPLETE_REQUEST_CODE_START)
        }
        destinationLocationEditText.setOnClickListener {
            startAutocompleteActivity(AUTOCOMPLETE_REQUEST_CODE_DESTINATION)
        }
    }

    // Set up destination list and add button for adding locations
    private fun setupDestinationList() {
        val destinationListView = findViewById<ListView>(R.id.destination_listView)
        val destinationLocationEditText = findViewById<EditText>(R.id.editTextDestLocation)
        val addButton = findViewById<Button>(R.id.add_button)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, destinationList)
        destinationListView.adapter = adapter

        addButton.setOnClickListener {
            val destinationText = destinationLocationEditText.text.toString()
            if (destinationText.isNotBlank()) {
                destinationList.add(destinationText)
                adapter.notifyDataSetChanged()
                destinationLocationEditText.text.clear()
                Toast.makeText(this, "Added: $destinationText", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please select a destination location first.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Set up Date Picker for selecting a date
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

    // Set up Time Picker for selecting time
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

    // Launch Google Places Autocomplete activity with specified request code
    private fun startAutocompleteActivity(requestCode: Int) {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this)
        startActivityForResult(intent, requestCode)
    }

    // Handle results from Autocomplete activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            val place = Autocomplete.getPlaceFromIntent(data)
            val locationEditText: EditText? = when (requestCode) {
                AUTOCOMPLETE_REQUEST_CODE_START -> findViewById(R.id.editTextStartLocation)
                AUTOCOMPLETE_REQUEST_CODE_DESTINATION -> findViewById(R.id.editTextDestLocation)
                else -> null
            }
            locationEditText?.setText(place.name)
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR && data != null) {
            val status = Autocomplete.getStatusFromIntent(data)
            Toast.makeText(this, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}
