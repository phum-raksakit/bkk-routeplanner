package com.example.bkkrouteplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter for displaying timeline items in the RecyclerView
class TimelinePlanAdapter(private val items: List<TimelineItem>) : RecyclerView.Adapter<TimelinePlanAdapter.TimelineViewHolder>() {

    // ViewHolder class for holding and binding views
    inner class TimelineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val placeName: TextView = view.findViewById(R.id.placeName) // TextView for place name
        val address: TextView = view.findViewById(R.id.address)     // TextView for place address
        val line: View = view.findViewById(R.id.line)               // View representing the timeline line
    }

    // Create and return a new ViewHolder for the item view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.plan_detail_card, parent, false)
        return TimelineViewHolder(view)
    }

    // Bind data to the views in the ViewHolder
    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val item = items[position]
        holder.placeName.text = item.placeName   // Set place name
        holder.address.text = item.address       // Set place address

        // Hide the timeline line for the last item in the list
        if (position == items.size - 1) {
            holder.line.visibility = View.INVISIBLE
        } else {
            holder.line.visibility = View.VISIBLE
        }
    }

    // Return the total number of items in the list
    override fun getItemCount(): Int = items.size
}
