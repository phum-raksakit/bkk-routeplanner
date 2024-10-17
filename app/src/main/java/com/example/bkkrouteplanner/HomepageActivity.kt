package com.example.bkkrouteplanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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

    private fun clearAllData() {
        val sharedPreferences = getSharedPreferences("myPlanStorage", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // ลบข้อมูลทั้งหมดใน SharedPreferences
        editor.apply()

        Toast.makeText(this, "All data cleared.", Toast.LENGTH_SHORT).show()
    }
}
