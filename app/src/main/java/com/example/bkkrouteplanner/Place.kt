package com.example.bkkrouteplanner

import com.google.android.libraries.places.api.model.OpeningHours

data class Place (
    val id:String,
    val name:String,
    val lat:Double,
    val lng:Double,
    val openingHours: List<String>?
)
