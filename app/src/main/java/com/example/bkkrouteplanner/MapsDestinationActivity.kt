package com.example.bkkrouteplanner

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bkkrouteplanner.databinding.ActivityMapsdestinationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class MapsDestinationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsdestinationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var searchMarker: Marker? = null
    private var destinationPlace: String? = null
    private lateinit var placesClient: PlacesClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Places API
        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(this)

        // Inflate layout using ViewBinding
        binding = ActivityMapsdestinationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Set up the map fragment and get notified when the map is ready to use
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up Google Places AutocompleteSupportFragment for place search
        val autocompleteFragment = supportFragmentManager
            .findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS_COMPONENTS)
                val request = FetchPlaceRequest.builder(place.id!!, placeFields).build()

                placesClient.fetchPlace(request).addOnSuccessListener { response ->
                    val placeDetail = response.place
                    val addressComponents = placeDetail.addressComponents?.asList()
                    var isInBangkok = false

                    // เช็ค postal code และตรวจสอบว่าอยู่ในกรุงเทพฯ หรือไม่
                    addressComponents?.forEach { component ->
                        if (component.types.contains("postal_code")) {
                            val postalCode = component.name.toIntOrNull()
                            if (postalCode != null && postalCode in 10000..10999) {
                                isInBangkok = true
                            }
                        }
                    }

                    if (isInBangkok) {
                        // ถ้าอยู่ในกรุงเทพฯ ให้เพิ่ม marker และเก็บชื่อสถานที่
                        searchMarker?.remove()
                        val latLng = place.latLng
                        if (latLng != null) {
                            searchMarker = mMap.addMarker(MarkerOptions().position(latLng).title(placeDetail.name))
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                            destinationPlace = place.name

                        }
                    } else {
                        Toast.makeText(this@MapsDestinationActivity, "Selected place is not in Bangkok.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this@MapsDestinationActivity, "Failed to retrieve place details.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(this@MapsDestinationActivity, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })

        setupBackButton()

    }

    // Set up the back button to return the selected start place
    private fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("DESTINATION_PLACE", destinationPlace)
            setResult(RESULT_OK, resultIntent)
            finish() // Close MapsActivity
        }
    }

    // This method is called when the map is ready to be used
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
}
