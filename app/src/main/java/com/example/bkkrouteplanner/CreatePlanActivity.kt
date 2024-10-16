package com.example.bkkrouteplanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class CreatePlanActivity : AppCompatActivity() {

    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE_START = 1
        private const val AUTOCOMPLETE_REQUEST_CODE_DESTINATION = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_plan)

        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        timePicker.setIs24HourView(true)

        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)

        // กำหนด OnClickListener ให้กับ EditText สำหรับ Start Location
        val startLocationEditText = findViewById<EditText>(R.id.StartLocation)
        startLocationEditText.setOnClickListener {
            startAutocompleteActivity(AUTOCOMPLETE_REQUEST_CODE_START)
        }

        // กำหนด OnClickListener ให้กับ EditText สำหรับ Destination Location
        val destinationLocationEditText = findViewById<EditText>(R.id.DestLocation)
        destinationLocationEditText.setOnClickListener {
            startAutocompleteActivity(AUTOCOMPLETE_REQUEST_CODE_DESTINATION)
        }
    }

    private fun startAutocompleteActivity(requestCode: Int) {
        // กำหนดประเภทของข้อมูลสถานที่ที่ต้องการ
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        // สร้าง Intent สำหรับเปิด Autocomplete Activity
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(this)

        // เปิด Autocomplete Activity โดยส่ง requestCode เพื่อแยกการทำงาน
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
                    // ตั้งค่าให้กับ EditText สำหรับ Start Location
                    locationEditText = findViewById(R.id.StartLocation)
                }
                AUTOCOMPLETE_REQUEST_CODE_DESTINATION -> {
                    // ตั้งค่าให้กับ EditText สำหรับ Destination Location
                    locationEditText = findViewById(R.id.DestLocation)
                }
                else -> return
            }

            locationEditText?.setText(placeName)
            Toast.makeText(this, "Selected: $placeName", Toast.LENGTH_SHORT).show()
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR && data != null) {
            // จัดการเมื่อเกิดข้อผิดพลาด
            val status = Autocomplete.getStatusFromIntent(data)
            Toast.makeText(this, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // ผู้ใช้ยกเลิกการเลือก
            Toast.makeText(this, "Selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}
