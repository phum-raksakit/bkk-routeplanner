package com.example.bkkrouteplanner

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PlanDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_plan_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewPlace)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val items = listOf(
            TimelineItem("SEA LIFE Bangkok", "Floor B1 Siam Paragon"),
            TimelineItem("Museum Siam", "4 Sanam Chai Rd"),
            TimelineItem("Dusit Palace", "71 U Thong Nai Alley")
        )
        val adapter = TimelinePlanAdapter(items)
        recyclerView.adapter = adapter
    }
}