package com.example.bkkrouteplanner

data class Trip (
    val id:String,
    val planname:String,
    val startMark:LatLng,
    val listLocation:List<Place>,
    val startDate:String,
    val endDate:String
)