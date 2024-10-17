package com.example.bkkrouteplanner

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomepageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        // find button by id=buttonAddNewPlan
        val buttonNewPlan = findViewById<FloatingActionButton>(R.id.buttonAddNewPlan)

        buttonNewPlan.setOnClickListener {
            // create intent to change activity
            val intent = Intent(this, CreatePlanActivity::class.java)
            startActivity(intent)
        }
    }
}
