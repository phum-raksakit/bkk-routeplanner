package com.example.bkkrouteplanner


data class LatLng(
    val latitude: Double,
    val longitude: Double
)

data class LatLngBounds (
    val southwest: LatLng,
    val northeast: LatLng
)