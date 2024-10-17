package com.example.bkkrouteplanner

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomepageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

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
