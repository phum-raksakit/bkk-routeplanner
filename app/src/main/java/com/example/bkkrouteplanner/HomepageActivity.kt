package com.example.bkkrouteplanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


//HomepageActivity is responsible for displaying a list of saved travel plans.
class HomepageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)
        //clearAllSharedPreferences()

        // Set up RecyclerView to display the list of saved plans
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewPlan)
        val noPlan: LinearLayout = findViewById(R.id.noPlan)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load saved plans from SharedPreferences
        val items = loadPlansFromSharedPreferences()

        // Create and set an adapter for the RecyclerView
        val adapter = PlanAdaptor(items) { planItem ->
            // When a plan is clicked, navigate to PlanDetailsActivity
            val intent = Intent(this, PlanDetailsActivity::class.java)
            intent.putExtra("PLAN_ID", planItem.planId) // Pass the planId to PlanDetailsActivity
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        // If no plans exist, display the "No plans" message
        if (adapter.itemCount == 0) {
            recyclerView.visibility = View.GONE
            noPlan.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            noPlan.visibility = View.GONE
        }

        // Set up the FloatingActionButton to add a new plan
        setupButtonAddNewPlan()
    }

    //Function to configure the FloatingActionButton to open CreatePlanActivity when clicked.
    private fun setupButtonAddNewPlan() {
        val buttonNewPlan = findViewById<FloatingActionButton>(R.id.buttonAddNewPlan)
        buttonNewPlan.setOnClickListener {
            val intent = Intent(this, CreatePlanActivity::class.java)
            startActivity(intent)
        }
    }


    //Function to load saved plans from SharedPreferences.
    private fun loadPlansFromSharedPreferences(): List<PlanItem> {
        val sharedPreferences = getSharedPreferences("PlanStorage", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("planList", "[]")

        Log.d("LoadCheck", "JSON loaded: $json") // Log the JSON data loaded

        val type = object : TypeToken<List<Plan>>() {}.type

        // Convert the JSON string into a list of Plan objects
        val planList: List<Plan> = gson.fromJson(json, type) ?: emptyList()

        Log.d("LoadCheck", "Plan list: $planList") // Log the list of Plan objects

        // Convert Plan objects into PlanItem objects for RecyclerView display
        return planList.map { plan ->
            val planItem = PlanItem(
                planId = plan.id,
                planName = plan.planName,
                dateOfPlan = plan.date,
                numOfPlaces = "${plan.destination.size} Places",
                place1 = plan.destination.firstOrNull() ?: "N/A",
                place2 = plan.destination.getOrNull(1) ?: "N/A"
            )
            return@map planItem
        }
    }

    //Function to clear all saved plans from SharedPreferences.
    private fun clearAllSharedPreferences() {
        val sharedPreferences = getSharedPreferences("PlanStorage", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.clear()
        editor.apply()

        Toast.makeText(this, "All saved plans have been cleared.", Toast.LENGTH_SHORT).show()
    }
}
