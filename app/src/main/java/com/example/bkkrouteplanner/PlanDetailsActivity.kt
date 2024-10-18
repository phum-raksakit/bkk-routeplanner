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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlanDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan_details)

        // Get the plan ID from the Intent extras
        val planId = intent.getStringExtra("PLAN_ID")
        if (planId != null) {
            displayPlanDetails(planId) // Display plan details for the provided ID
        } else {
            Log.d("SaveCheck", "Plan ID not provided.") // Log if no plan ID is provided
        }

        // Set up the back button to navigate back to HomepageActivity
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, HomepageActivity::class.java)
            startActivity(intent)
        }
    }

    // Function to display plan details based on the given plan ID
    private fun displayPlanDetails(planId: String) {
        val sharedPreferences = getSharedPreferences("PlanStorage", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("planList", "[]") // Get saved plan list from SharedPreferences
        val type = object : TypeToken<List<Plan>>() {}.type
        val planList: List<Plan> = gson.fromJson(json, type) ?: emptyList()

        // Find the specific plan using the plan ID
        val plan = planList.find { it.id == planId }

        if (plan != null) {
            // Set the TextViews with data from the plan
            findViewById<TextView>(R.id.textViewPlanName).text = plan.planName // Set plan name
            findViewById<TextView>(R.id.textViewMap).text = plan.start         // Set starting point
            findViewById<TextView>(R.id.textViewDate).text = plan.date         // Set date
            findViewById<TextView>(R.id.textViewTime).text = plan.time         // Set time

            // Set up RecyclerView for displaying the destinations
            val recyclerView: RecyclerView = findViewById(R.id.recyclerViewPlace)
            recyclerView.layoutManager = LinearLayoutManager(this)             // Set layout manager

            // Convert destinations to TimelineItem list and set up the adapter
            val items = plan.destination.map { TimelineItem(it, "") }          // Convert destinations to TimelineItem
            val adapter = TimelinePlanAdapter(items)
            recyclerView.adapter = adapter                                     // Set adapter to RecyclerView
        } else {
            Log.d("SaveCheck", "Plan with ID $planId not found.")              // Log if plan is not found
        }
    }
}
