package com.example.bkkrouteplanner

import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlanAdaptor(private val items: List<PlanItem>) : RecyclerView.Adapter<PlanAdaptor.PlanViewHolder>() {

    inner class PlanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val planName: TextView = view.findViewById(R.id.PlanName)
        val dateOfPlan: TextView = view.findViewById(R.id.DateOfPlan)
        val numOfPlace: TextView = view.findViewById(R.id.NumOfPlace)
        val place1: TextView = view.findViewById(R.id.Place1)
        val place2: TextView = view.findViewById(R.id.Place2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itinerary_plan_card, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val item = items[position]
        holder.planName.text = item.planName
        holder.dateOfPlan.text = item.dateOfPlan
        holder.numOfPlace.text = item.numOfPlace
        holder.place1.text = item.place1
        holder.place2.text = item.place2

    }

    override fun getItemCount(): Int = items.size
}