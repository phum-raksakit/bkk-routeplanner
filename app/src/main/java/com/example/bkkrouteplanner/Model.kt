package com.example.bkkrouteplanner

data class Place (
    val id:String,
    val name:String,
    val lat:Double,
    val lng:Double,
    val openingHours: List<String>?
)

data class Trip (
    val id:String,
    val planName:String,
    val startMark:LatLng,
    val listLocation:List<Place>,
    val startDate:String,
    val endDate:String
)

data class LatLng(
    val latitude: Double,
    val longitude: Double
)

data class LatLngBounds (
    val southwest: LatLng,
    val northeast: LatLng
)
