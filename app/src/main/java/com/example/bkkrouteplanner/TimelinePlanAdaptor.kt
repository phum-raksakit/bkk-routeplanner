package com.example.bkkrouteplanner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimelinePlanAdapter(private val items: List<TimelineItem>) : RecyclerView.Adapter<TimelinePlanAdapter.TimelineViewHolder>() {

    inner class TimelineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val placeName: TextView = view.findViewById(R.id.placeName)
        val address: TextView = view.findViewById(R.id.address)
        val line: View = view.findViewById(R.id.line)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.plan_detail_card, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val item = items[position]
        holder.placeName.text = item.placeName
        holder.address.text = item.address

        // hide line if it's last item
        if (position == items.size - 1) {
            holder.line.visibility = View.INVISIBLE
        } else {
            holder.line.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = items.size
}