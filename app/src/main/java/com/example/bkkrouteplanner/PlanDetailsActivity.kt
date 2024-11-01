package com.example.bkkrouteplanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlin.math.log

class PlanDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var mapReady = false
    private var plan: Plan? = null
    private var startPlace: place? = null
    private lateinit var placesClient: PlacesClient
    private var currentPlanForStorage: PlanForStorage? = null
    private val latLngList = mutableListOf<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan_details)

        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(this)

        // Initialize the map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val planId = intent.getStringExtra("PLAN_ID")

        if (planId != null) {
            displayPlanDetails(planId)
        } else {
            Log.d("SaveCheck", "Plan ID not provided.")
        }

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, HomepageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun displayPlanDetails(planId: String) {
        val sharedPreferences = getSharedPreferences("PlanStorage", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("planList", "[]")
        val type = object : TypeToken<List<PlanForStorage>>() {}.type
        val planList: List<PlanForStorage> = gson.fromJson(json, type) ?: emptyList()

        val plan = planList.find { it.id == planId }
        currentPlanForStorage = plan
        plan?.let { currentPlan ->
            // Load and display basic information
            findViewById<TextView>(R.id.textViewPlanName).text = currentPlan.planName
            findViewById<TextView>(R.id.textViewMap).text = currentPlan.start?.name
            findViewById<TextView>(R.id.textViewDate).text = currentPlan.date
            findViewById<TextView>(R.id.textViewTime).text = currentPlan.time

            Log.d("Check Log", "$currentPlan")

            // Display itinerary in RecyclerView
            val recyclerView: RecyclerView = findViewById(R.id.recyclerViewPlace)
            recyclerView.layoutManager = LinearLayoutManager(this)

            // Create list of timeline items from itinerary
            val items = currentPlan.itinerary.map {
                val placeName = it.first.name
                val time = it.second
                TimelineItem(placeName, time)
            }

            val adapter = TimelinePlanAdapter(items)
            recyclerView.adapter = adapter

        } ?: run {
            Log.d("SaveCheck", "Plan with ID $planId not found.")
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapReady = true

        currentPlanForStorage?.let { markMapWithPlacesById(it) }
    }

    private fun fetchPlaceById(placeId: String, onResult: (LatLng?) -> Unit) {
        val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                Log.d("PlaceInfo", "Place found: ${place.name}, Location: ${place.latLng}")
                onResult(place.latLng)
            }
            .addOnFailureListener { exception ->
                Log.e("PlaceInfo", "Place not found: ${exception.message}")
                onResult(null)
            }
    }

    private fun markMapWithPlacesById(plan: PlanForStorage) {
        val boundsBuilder = LatLngBounds.Builder()
        latLngList.clear()  // Clear previous entries

        val onLatLngFetched: (LatLng?) -> Unit = { latLng ->
            latLng?.let {
                latLngList.add(it)
                boundsBuilder.include(it)
            }
            // Check if all points have been processed
            if (latLngList.size == 1 + plan.itinerary.size) {  // 1 for start place + itinerary size
                if (latLngList.isNotEmpty()) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
                } else {
                    Log.e("MapError", "No valid locations to display on map")
                }
            }
        }

        // Fetch start place
        plan.start?.let { startPlace ->
            fetchPlaceById(startPlace.id) { latLng ->
                latLng?.let {
                    mMap.addMarker(MarkerOptions().position(it).title("Start: ${startPlace.name}"))
                    onLatLngFetched(it)
                }
            }
        }

        // Fetch each destination in itinerary
        plan.itinerary.forEach { (place, time) ->
            fetchPlaceById(place.id) { latLng ->
                latLng?.let {
                    mMap.addMarker(MarkerOptions().position(it).title("${place.name} at $time"))
                    onLatLngFetched(it)
                }
            }
        }
    }
}
