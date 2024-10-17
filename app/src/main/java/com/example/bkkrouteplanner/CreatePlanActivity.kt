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

        // Initialize Places API
        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)

        // TimePicker in 24-hour format

        // EditText for Start Location with Autocomplete
        val startLocationEditText = findViewById<EditText>(R.id.editTextStartLocation)
        startLocationEditText.setOnClickListener {
            startAutocompleteActivity(AUTOCOMPLETE_REQUEST_CODE_START)
        }

        // EditText for Destination Location with Autocomplete
        val destinationLocationEditText = findViewById<EditText>(R.id.editTextDestLocation)
        destinationLocationEditText.setOnClickListener {
            startAutocompleteActivity(AUTOCOMPLETE_REQUEST_CODE_DESTINATION)
        }

        // ListView and Adapter for Destination List
        val destinationListView = findViewById<ListView>(R.id.destination_listView)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, destinationList)
        destinationListView.adapter = adapter

        // Add Button for Destination List
        val addButton = findViewById<Button>(R.id.add_button)
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

        // Material DatePicker for Date Selection
        val dateEditText = findViewById<EditText>(R.id.editTextDate)
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a Date")
            .build()

        dateEditText.setOnClickListener {
            datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        }

        datePicker.addOnPositiveButtonClickListener { selection ->
            dateEditText.setText(datePicker.headerText)
            Toast.makeText(this, "Selected Date: ${datePicker.headerText}", Toast.LENGTH_SHORT).show()
        }

        // Material TimePicker for Time Selection
        val timeEditText = findViewById<EditText>(R.id.editTextTime)
        val timePickerMaterial = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Appointment time")
            .build()

        timeEditText.setOnClickListener {
            timePickerMaterial.show(supportFragmentManager, "MATERIAL_TIME_PICKER")
        }

        timePickerMaterial.addOnPositiveButtonClickListener {
            val selectedHour = timePickerMaterial.hour
            val selectedMinute = timePickerMaterial.minute
            timeEditText.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
            Toast.makeText(this, "Selected Time: $selectedHour:$selectedMinute", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAutocompleteActivity(requestCode: Int) {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            val place = Autocomplete.getPlaceFromIntent(data)
            val placeName = place.name
            val locationEditText: EditText?

            when (requestCode) {
                AUTOCOMPLETE_REQUEST_CODE_START -> {
                    locationEditText = findViewById(R.id.editTextStartLocation)
                }
                AUTOCOMPLETE_REQUEST_CODE_DESTINATION -> {
                    locationEditText = findViewById(R.id.editTextDestLocation)
                }
                else -> return
            }

            locationEditText?.setText(placeName)
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR && data != null) {
            val status = Autocomplete.getStatusFromIntent(data)
            Toast.makeText(this, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}
