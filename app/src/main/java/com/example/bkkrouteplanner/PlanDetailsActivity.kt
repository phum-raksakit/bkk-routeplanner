package com.example.bkkrouteplanner

import android.content.Context
import android.os.Bundle
import android.util.Log
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

        // ดึงแผนตาม ID ที่ต้องการ
        val planId = "Plan_1729199517171" // ตัวอย่าง ID
        if (planId != null) {
            displayPlanDetails(planId)
        } else {
            Log.d("SaveCheck", "Plan ID not provided.")
        }
    }

    private fun displayPlanDetails(planId: String) {
        val sharedPreferences = getSharedPreferences("myPlanStorage", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("planList", "[]")
        val type = object : TypeToken<List<Plan>>() {}.type
        val planList: List<Plan> = gson.fromJson(json, type) ?: emptyList()

        val plan = planList.find { it.id == planId }

        if (plan != null) {

            // ตั้งค่า TextView จากข้อมูลที่โหลดมา
            findViewById<TextView>(R.id.textViewPlanName).text = plan.planName
            findViewById<TextView>(R.id.textViewMap).text = plan.start
            findViewById<TextView>(R.id.textViewDate).text = plan.date
            findViewById<TextView>(R.id.textViewTime).text = plan.time

            // ตั้งค่า RecyclerView สำหรับ destination
            val recyclerView: RecyclerView = findViewById(R.id.recyclerViewReview)
            recyclerView.layoutManager = LinearLayoutManager(this)

            // สร้างรายการ TimelineItem จาก destination
            val items = plan.destination.map { TimelineItem(it, "") } // map เพื่อแปลงเป็น TimelineItem
            val adapter = TimelinePlanAdapter(items)
            recyclerView.adapter = adapter
        } else {
            Log.d("SaveCheck", "Plan with ID $planId not found.")
        }
    }
}


