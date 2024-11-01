package com.example.bkkrouteplanner

import android.os.Parcelable
import com.google.maps.model.LatLng
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

data class place(
    var id: String,
    var name : String
)

data class Plan(
    val id:String,
    val planName:String,
    val start:place?,
    val latlng : com.google.android.gms.maps.model.LatLng?,
    val destination: MutableList<place>,
    val date:String,
    val time:String,
    val itinerary: MutableList<Pair<place, LocalTime>>?
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
    val time: String,
)

data class PopularTime(
    val day: String,
    val hours: List<TimeSlot>
)

data class TimeSlot(
    val hour: Int,
    val popularity: Int
)

data class PlaceInfo(
    val id: String,
    val name: String?,
    val openingHours: String,
    val popularTimes: List<PopularTime>,
    var score: Int = 0
)
data class PlanForStorage(
    val id: String,
    val planName: String,
    val start: place?,
    val latlng: com.google.android.gms.maps.model.LatLng?,
    val date: String,
    val time: String,
    val itinerary: MutableList<Pair<place, String>> // ใช้ String แทน LocalTime
)

