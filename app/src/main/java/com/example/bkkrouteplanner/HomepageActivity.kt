package com.example.bkkrouteplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomepageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewPlan)
        val noPlan: LinearLayout = findViewById(R.id.noPlan)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val items = listOf(
            PlanItem("Plan 1", "20 OCT 2024", "13 Places", "Suan Luang Rama IX,", "Dusit Palace, ..."),
            PlanItem("Plan 2", "17 OCT 2024", "6 Places", "Suan Luang Rama IX,", "Dusit Palace, ..."),
            PlanItem("Plan 3", "9 OCT 2024", "3 Places", "Suan Luang Rama IX,", "Dusit Palace, ...")
        )

        // val items = listOf<PlanItem>()

        val adapter = PlanAdaptor(items)
        recyclerView.adapter = adapter

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

    // Function to configure the FloatingActionButton for opening CreatePlanActivity when clicked
    private fun setupButtonAddNewPlan() {
        val buttonNewPlan = findViewById<FloatingActionButton>(R.id.buttonAddNewPlan)

        buttonNewPlan.setOnClickListener {
            val intent = Intent(this, CreatePlanActivity::class.java)
            startActivity(intent)
        }
    }

}
