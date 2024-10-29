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
    private var startPlace: PlaceData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan_details)

        // Initialize the map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val planId = intent.getStringExtra("PLAN_ID")

        if (planId != null) {
            displayPlanDetails(planId) // โหลดข้อมูลแผนตาม PLAN_ID ที่ได้รับมา
        } else {
            Log.d("SaveCheck", "Plan ID not provided.") // Log ถ้าไม่มี PLAN_ID
        }

        // ปุ่มย้อนกลับเพื่อกลับไปที่หน้า HomepageActivity
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
        val type = object : TypeToken<List<Plan>>() {}.type
        val planList: List<Plan> = gson.fromJson(json, type) ?: emptyList()

        plan = planList.find { it.id == planId }

        plan?.let { currentPlan ->
            loadPlaces(currentPlan)
            findViewById<TextView>(R.id.textViewPlanName).text = currentPlan.planName
            findViewById<TextView>(R.id.textViewMap).text = currentPlan.start?.name
            findViewById<TextView>(R.id.textViewDate).text = currentPlan.date
            findViewById<TextView>(R.id.textViewTime).text = currentPlan.time

            // แสดงสถานที่ใน RecyclerView
            val recyclerView: RecyclerView = findViewById(R.id.recyclerViewPlace)
            recyclerView.layoutManager = LinearLayoutManager(this)

            val items = currentPlan.destination.map { TimelineItem(it.name, "") }
            val adapter = TimelinePlanAdapter(items)
            recyclerView.adapter = adapter


        } ?: run {
            Log.d("SaveCheck", "Plan with ID $planId not found.") // Log ถ้าไม่พบแผนการเดินทางที่มี PLAN_ID นี้
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapReady = true

    }

    private fun loadPlaces(plan: Plan) {
        val sharedPreferences = getSharedPreferences("PlaceStorage", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("placeList", "[]") // Load the list of places from SharedPreferences
        val type = object : TypeToken<List<PlaceData>>() {}.type
        val placeList: List<PlaceData> = gson.fromJson(json, type) ?: emptyList()

        // Find startPlace using the start ID from the plan
        startPlace = placeList.find { it.id == plan.start?.id }

        startPlace?.let { place ->
            Log.d("StartPlace", "Loaded start place: ${place.name}")
        } ?: run {
            Log.e("StartPlace", "Start place with ID ${plan.start} not found.")
        }

        // Load destinations directly from the plan's destination list
        val destinations = plan.destination // This is already a list of PlaceInfo

        if (destinations.isNotEmpty()) {
            Log.d("Destinations", "Loaded ${destinations.size} destinations.")
            destinations.forEach { place ->
                Log.d("Destination", "Loaded destination: ${place.name}")
            }
        } else {
            Log.e("Destinations", "No destinations found for the provided plan.")
        }
    }
}
