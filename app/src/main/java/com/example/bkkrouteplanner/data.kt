package com.example.bkkrouteplanner

data class Place (
    val id:String,
    val name:String,
    val lat:Double,
    val lng:Double,
    val openingHours: List<String>?
)

//data class Trip (
//    val id:String,
//    val planName:String,
//    val startMark:LatLng,
//    val listLocation:List<Place>,
//    val startDate:String,
//    val endDate:String
//)

data class Plan(
    val id:String,
    val planName:String,
    val start:String,
    val destination: MutableList<String>,
    val date:String,
    val time:String
)

data class LatLng(
    val latitude: Double,
    val longitude: Double
)

