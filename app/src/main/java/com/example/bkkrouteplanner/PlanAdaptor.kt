package com.example.bkkrouteplanner

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter for displaying a list of PlanItem in a RecyclerView
class PlanAdaptor(
    private val items: List<PlanItem>,                  // List of items to display
    private val onItemClick: (PlanItem) -> Unit,         // Callback for handling item clicks
    private val onDeleteClick: (PlanItem) -> Unit       // Callback for handling delete actions
) : RecyclerView.Adapter<PlanAdaptor.PlanViewHolder>() {

    // ViewHolder class to hold the views for each item in the list
    inner class PlanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val planName: TextView = view.findViewById(R.id.PlanName)         // TextView for the plan name
        val dateOfPlan: TextView = view.findViewById(R.id.DateOfPlan)     // TextView for the plan date
        val numOfPlace: TextView = view.findViewById(R.id.NumOfPlace)     // TextView for the number of places
        val place1: TextView = view.findViewById(R.id.Place1)             // TextView for the first place
        val place2: TextView = view.findViewById(R.id.Place2)             // TextView for the second place
        val deleteButton: ImageButton = view.findViewById(R.id.DeletePlanButton) // Delete button
    }

    // Creates a new ViewHolder when there are no existing ViewHolders available for reuse
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.itinerary_plan_card, parent, false)        // Inflate the layout for each item
        return PlanViewHolder(view)                                      // Return the new ViewHolder
    }

    // Binds data to the ViewHolder at the given position
    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val item = items[position]                                       // Get the current item
        holder.planName.text = item.planName                             // Set plan name
        holder.dateOfPlan.text = item.dateOfPlan                         // Set plan date
        holder.numOfPlace.text = item.numOfPlaces                        // Set number of places
        holder.place1.text = item.place1                                 // Set first place
        holder.place2.text = item.place2                                 // Set second place

        // Set OnClickListener for handling item clicks
        holder.itemView.setOnClickListener {
            onItemClick(item)                                            // Trigger callback on item click
        }

        // Handle delete button click
        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(holder.itemView.context, item)
        }
    }

    // Show confirmation dialog for deletion
    private fun showDeleteConfirmationDialog(context: Context, item: PlanItem) {
        AlertDialog.Builder(context).apply {
            setTitle("Delete Plan")
            setMessage("Are you sure you want to delete this plan?")
            setPositiveButton("Yes") { _, _ ->
                onDeleteClick(item)      // Trigger deletion callback
            }
            setNegativeButton("No", null)
            show()
        }
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int = items.size
}
