package com.example.bkkrouteplanner

import android.os.Parcelable
import com.google.maps.model.LatLng
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

data class place(
    var id: String,
    var name : String
)

@Parcelize
data class PlaceData(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val openingHours: MutableList<String>? = mutableListOf(),
    var duration: Long = Long.MAX_VALUE,
    val popularTimes: MutableList<Pair<Long, Long>>? = mutableListOf(),
    val priority: Int = 1
) : Parcelable {
    override fun toString(): String {
        return name
    }

    fun isOpenAt(currentTime: Long): Boolean {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTimeCalendar = Calendar.getInstance().apply { timeInMillis = currentTime }

        openingHours?.forEach { timeRange ->
            val times = timeRange.split("-")
            if (times.size == 2) {
                val openTime = format.parse(times[0])?.time ?: return false
                val closeTime = format.parse(times[1])?.time ?: return false

                if (currentTimeCalendar.timeInMillis in openTime..closeTime) {
                    return true
                }
            }
        }
        return false
    }

    fun isPopularTime(currentTime: Long): Boolean {
        return popularTimes?.any { (start, end) ->
            currentTime in start..end
        } ?: false
    }
}

data class Plan(
    val id:String,
    val planName:String,
    val start:place?,
    val latlng : com.google.android.gms.maps.model.LatLng?,
    val destination: MutableList<place>,
    val date:String,
    val time:String
)

data class PlanItem(
    val planId: String,
    val planName: String,
    val dateOfPlan: String,
    val numOfPlaces: String,
    val place1: String,
    val place2: String
)

data class TimelineItem(
    val placeName: String,
    val address: String
)

