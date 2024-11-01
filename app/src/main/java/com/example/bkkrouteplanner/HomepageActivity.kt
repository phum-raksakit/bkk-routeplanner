package com.example.bkkrouteplanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomepageActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 1001
    }

    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)
        //clearAllSharedPreferences()

        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(this)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewPlan)
        val noPlan: LinearLayout = findViewById(R.id.noPlan)
        recyclerView.layoutManager = LinearLayoutManager(this@HomepageActivity)

        val items = loadPlansFromSharedPreferences()
        Log.d("Check Plan", "${items}")
        val adapter = PlanAdaptor(items, onItemClick = { planItem ->
            val intent = Intent(this, PlanDetailsActivity::class.java)
            intent.putExtra("PLAN_ID", planItem.planId)
            startActivity(intent)
        }, onDeleteClick = { planItem ->
            deletePlanFromSharedPreferences(planItem.planId)
            recreate()
        })
        recyclerView.adapter = adapter

        if (adapter.itemCount == 0) {
            recyclerView.visibility = View.GONE
            noPlan.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            noPlan.visibility = View.GONE
        }
        setupButtonAddNewPlan()
        loadFileFromSharedPreferences()
    }

    private fun setupButtonAddNewPlan() {
        val buttonNewPlan = findViewById<FloatingActionButton>(R.id.buttonAddNewPlan)
        buttonNewPlan.setOnClickListener {
            val intent = Intent(this, CreatePlanActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE -> {
                    recreate()
                }
            }
        }
    }

    private fun loadPlansFromSharedPreferences(): List<PlanItem> {
        val sharedPreferences = getSharedPreferences("PlanStorage", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("planList", "[]")

        Log.d("LoadCheck", "JSON loaded: $json")

        val type = object : TypeToken<List<PlanForStorage>>() {}.type
        val planForStorageList: List<PlanForStorage> = gson.fromJson(json, type) ?: emptyList()

        Log.d("LoadCheck", "PlanForStorage list: $planForStorageList")

        return planForStorageList.map { storedPlan ->
            val place1 = storedPlan.itinerary.getOrNull(0)?.first?.name ?: "Unknown Place 1"
            val place2 = storedPlan.itinerary.getOrNull(1)?.first?.name ?: "Unknown Place 2"

            PlanItem(
                planId = storedPlan.id,
                planName = storedPlan.planName,
                dateOfPlan = storedPlan.date,
                numOfPlaces = "${storedPlan.itinerary.size} Places",
                place1 = place1,
                place2 = place2
            )
        }
    }

    private fun deletePlanFromSharedPreferences(planId: String) {
        val sharedPreferences = getSharedPreferences("PlanStorage", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = sharedPreferences.getString("planList", "[]")

        val type = object : TypeToken<MutableList<Plan>>() {}.type
        val planList: MutableList<Plan> = gson.fromJson(json, type) ?: mutableListOf()

        planList.removeAll { it.id == planId }

        editor.putString("planList", gson.toJson(planList))
        editor.apply()
    }

    private fun loadFileFromSharedPreferences() {
        // เข้าถึง SharedPreferences
        val sharedPreferences = getSharedPreferences("PlanStorage", Context.MODE_PRIVATE)

        // โหลดข้อมูลจาก SharedPreferences (ในที่นี้เป็น string)
        val storedData = sharedPreferences.getString("planList", "[]")

        // แสดงข้อมูลใน Logcat
        Log.d("LoadFile", "Data loaded from SharedPreferences: $storedData")
    }
}
