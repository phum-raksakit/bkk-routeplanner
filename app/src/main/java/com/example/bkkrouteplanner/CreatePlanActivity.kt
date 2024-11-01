package com.example.bkkrouteplanner

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class CreatePlanActivity : AppCompatActivity() {

    companion object {
        private const val MAPS_ACTIVITY_REQUEST_CODE_START = 1
        private const val MAPS_ACTIVITY_REQUEST_CODE_DEST = 2
    }

    private var startPlace: place? = null
    private var startPlaceID: String? = null
    private var currentLatLng: LatLng? = null
    private var destPlaceID: String? = null
    private lateinit var placesClient: PlacesClient
    private val destList: MutableList<place> = mutableListOf()
    private lateinit var destAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_plan)

        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(this)


        initializeUI()
    }

    private fun initializeUI() {
        destAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, destList.map { it.name })
        findViewById<ListView>(R.id.destination_listView).adapter = destAdapter

        setupDatePicker()
        setupTimePicker()
        setupDestButton()
        setupAddButton()
        setupStartButton()
        setupCreateButton()
        setupBackButton()
    }

    private fun setupStartButton(){
        val startButton = findViewById<EditText>(R.id.editTextStartLocation)
        startButton.setOnClickListener{
            val intent = Intent(this, MapsStartActivity::class.java)
            startActivityForResult(intent, MAPS_ACTIVITY_REQUEST_CODE_START)
        }
    }

    private fun setupAddButton() {
        val addButton = findViewById<Button>(R.id.add_button)
        addButton.setOnClickListener {
            addDestination()
            findViewById<EditText>(R.id.editTextDestLocation).text.clear()
        }
    }

    private fun setupDestButton(){
        val destButton = findViewById<EditText>(R.id.editTextDestLocation)
        destButton.setOnClickListener{
            val intent = Intent(this, MapsDestinationActivity::class.java)
            startActivityForResult(intent, MAPS_ACTIVITY_REQUEST_CODE_DEST)
        }
    }

    private fun setupDatePicker() {
        val dateEditText = findViewById<EditText>(R.id.editTextDate)
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .build()

        dateEditText.setOnClickListener {
            datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        }

        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDateFormatted = formatDate(selection)
            dateEditText.setText(selectedDateFormatted)
        }
    }

    private fun setupTimePicker() {
        val timeEditText = findViewById<EditText>(R.id.editTextTime)
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)  // Set to 12-hour format
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Appointment time")
            .build()

        timeEditText.setOnClickListener {
            timePicker.show(supportFragmentManager, "MATERIAL_TIME_PICKER")
        }

        timePicker.addOnPositiveButtonClickListener {
            // Format the selected time in 12-hour format with AM/PM
            val hour = timePicker.hour
            val minute = timePicker.minute
            val amPm = if (hour >= 12) "PM" else "AM"
            val formattedTime = String.format("%02d:%02d %s", if (hour % 12 == 0) 12 else hour % 12, minute, amPm)
            timeEditText.setText(formattedTime)
        }
    }

    private fun setupCreateButton(){
        val createButton = findViewById<Button>(R.id.create_button)
        val id = System.currentTimeMillis().toString()
        createButton.setOnClickListener{

            val plan = Plan(
                id = id,
                planName = findViewById<EditText>(R.id.editTextPlanName).text.toString(),
                start = startPlace,
                latlng = currentLatLng,
                destination = destList,
                date = findViewById<EditText>(R.id.editTextDate).text.toString(),
                time = findViewById<EditText>(R.id.editTextTime).text.toString(),
                itinerary = null
            )

            CoroutineScope(Dispatchers.IO).launch {
                val model = Model()
                model.initializePlacesClient(this@CreatePlanActivity) // Initialize placesClient here
                model.makePlan(plan)
                val trip = model.getTrip()
                Log.d("Trip","$trip")
                savePlanToLocalStorage(trip)
                val resultIntent = Intent(this@CreatePlanActivity, PlanDetailsActivity::class.java)
                resultIntent.putExtra("PLAN_ID", id)
                startActivity(resultIntent)
                finish()
            }
        }
    }

    private fun savePlanToLocalStorage(plan: Plan) {
        val sharedPreferences = getSharedPreferences("PlanStorage", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val newPlan = PlanForStorage(
            id = plan.id,
            planName = plan.planName,
            start = plan.start,
            latlng = plan.latlng,
            date= plan.date,
            time= plan.time,
            itinerary = plan.itinerary?.map { Pair(it.first, it.second.toString()) }?.toMutableList() ?: mutableListOf()
        )

        val planListJson = sharedPreferences.getString("planList", "[]")
        val planList: MutableList<PlanForStorage> = gson.fromJson(planListJson, object : TypeToken<MutableList<PlanForStorage>>() {}.type)
        planList.add(newPlan)

        editor.putString("planList", gson.toJson(planList))
        editor.apply()
    }

    private fun setupBackButton(){
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener{
            finish()
        }
    }

    private fun updateStartText(placeName: String?,id: String?) {
        startPlace = place(
            id = id!!,
            name = placeName!!
        )
        if (!placeName.isNullOrEmpty()) {
            findViewById<EditText>(R.id.editTextStartLocation).setText(placeName)
            Toast.makeText(this, "Start Place: $placeName", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to fetch start place", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDestText(placeName: String?) {
        if (!placeName.isNullOrEmpty()) {
            findViewById<EditText>(R.id.editTextDestLocation).setText(placeName)
            Toast.makeText(this, "Destination Place: $placeName", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to fetch destination place", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAdapter() {
        destAdapter.clear()
        destAdapter.addAll(destList.map { it.name })
        destAdapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                MAPS_ACTIVITY_REQUEST_CODE_START -> {
                    startPlaceID = data?.getStringExtra("START_PLACE_ID")

                    if (startPlaceID.isNullOrEmpty()) {
                        val latLngString = data?.getStringExtra("START_PLACE_LL")
                        updateStartText("Current Location",null)

                        latLngString?.let { latLng ->
                            val latLngParts = latLng.split(",")
                            if (latLngParts.size == 2) {
                                val latitude = latLngParts[0].toDoubleOrNull()
                                val longitude = latLngParts[1].toDoubleOrNull()
                                if (latitude != null && longitude != null) {
                                    currentLatLng = LatLng(latitude, longitude)
                                    Log.d("Start Location", "Received LatLng: $currentLatLng")
                                } else {
                                    Toast.makeText(this, "Invalid Latitude or Longitude", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "No valid LatLng received", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        startPlaceID?.let { id ->
                            fetchPlaceById(id) { placeName ->
                                if (placeName != null) {
                                    updateStartText(placeName,id)
                                } else {
                                    Toast.makeText(this, "Failed to fetch start name", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } ?: run {
                            Toast.makeText(this, "No start ID received", Toast.LENGTH_SHORT).show()
                        }
                        Log.d("Start Location", "Received Place ID: ${startPlace?.id}")
                    }
                }
                MAPS_ACTIVITY_REQUEST_CODE_DEST -> {
                    destPlaceID = data?.getStringExtra("DESTINATION_PLACE_ID")

                    destPlaceID?.let { id ->
                        fetchPlaceById(id) { placeName ->
                            if (placeName != null) {
                                updateDestText(placeName)
                            } else {
                                Toast.makeText(this, "Failed to fetch destination name", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } ?: run {
                        Toast.makeText(this, "No destination ID received", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun addDestination() {
        if (!destPlaceID.isNullOrEmpty()) {
            fetchPlaceById(destPlaceID!!) { placeName ->
                if (!placeName.isNullOrEmpty()) {
                    val newDestination = place(
                        id = destPlaceID!!,
                        name = placeName
                    )
                    Log.d("test", "Updated Destination List: ${newDestination.name}")
                    destList.add(newDestination)
                    updateAdapter()
                    Toast.makeText(this, "Destination added: ${newDestination.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to fetch place name", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "No destination selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchPlaceById(placeId: String, callback: (String?) -> Unit) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            callback(place.name)
        }.addOnFailureListener { exception ->
            Log.e("Place Fetch", "Error fetching place details: ${exception.message}")
            callback(null)
        }
    }

    private fun formatDate(dateInMillis: Long?): String {
        return dateInMillis?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
        } ?: ""
    }

}
