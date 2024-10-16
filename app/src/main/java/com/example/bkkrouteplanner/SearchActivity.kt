package com.example.bkkrouteplanner

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import android.widget.ListView
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class SearchActivity : AppCompatActivity() {

    private val listLocation: MutableList<com.example.bkkrouteplanner.Place> = mutableListOf()
    private lateinit var adapter: ArrayAdapter<String>
    private val listLocationDisplay: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val listView: ListView = findViewById(R.id.listView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listLocationDisplay)
        listView.adapter = adapter

        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)

        val autocompleteFragment = supportFragmentManager
            .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.OPENING_HOURS))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val placeId = place.id
                val placeName = place.name
                val latLng = place.latLng
                val openingHours = place.openingHours?.weekdayText

                if (openingHours != null) {
                    val openingHoursText = openingHours.joinToString(separator = "\n")
                    Toast.makeText(this@SearchActivity, "Place: $placeName, Opening Hours: $openingHoursText", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@SearchActivity, "Place: $placeName, No opening hours available", Toast.LENGTH_LONG).show()
                }

                savePlaceData(placeId, placeName, latLng?.latitude, latLng?.longitude, openingHours)
                adapter.notifyDataSetChanged()
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(this@SearchActivity, "Error : ${status.statusMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun savePlaceData(
        placeId: String?,
        placeName: String?,
        lat: Double?,
        lng: Double?,
        openingHours: List<String>?
    ) {
        if (placeId != null && placeName != null && lat != null && lng != null)
        {
            val place = com.example.bkkrouteplanner.Place(placeId, placeName, lat, lng, openingHours)
            listLocation.add(place)

            val placeText = "$placeName"
            listLocationDisplay.add(placeText)

            Toast.makeText(this, "Save Done", Toast.LENGTH_SHORT).show()
        }
    }
}