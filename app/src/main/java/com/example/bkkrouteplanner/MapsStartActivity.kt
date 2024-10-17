package com.example.bkkrouteplanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bkkrouteplanner.databinding.ActivityMapsstartBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import android.location.Geocoder
import java.util.Locale

class MapsStartActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsstartBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var searchMarker: Marker? = null
    private var startPlace: String? = null
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
        binding = ActivityMapsstartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FusedLocationProviderClient for location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
                            startPlace = place.name

                        }
                    } else {
                        Toast.makeText(this@MapsStartActivity, "Selected place is not in Bangkok.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this@MapsStartActivity, "Failed to retrieve place details.", Toast.LENGTH_SHORT).show()
                }
            }





            override fun onError(status: com.google.android.gms.common.api.Status) {
                Toast.makeText(this@MapsStartActivity, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
            }
        })

        setupBackButton()

        // Set up the mark location button to find the current location
        val markLocationButton = findViewById<Button>(R.id.currentLocationButton)
        markLocationButton.setOnClickListener {
            checkLocationPermission()
        }
    }

    // Set up the back button to return the selected start place
    private fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("START_PLACE", startPlace)
            setResult(RESULT_OK, resultIntent)
            finish() // Close MapsActivity
        }
    }

    // Check if location permissions are granted; if not, request permissions
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            showCurrentLocation()
        }
    }

    // Fetch and display the current location on the map
    private fun showCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)

                    // Use Geocoder to get the address from latitude and longitude
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]
                        val postalCode = address.postalCode?.toIntOrNull()

                        // Check if the postal code is within Bangkok's range
                        if (postalCode != null && postalCode in 10000..10999) {
                            mMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location (Bangkok)"))
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                            Toast.makeText(this, "You are in Bangkok!", Toast.LENGTH_SHORT).show()
                            startPlace = "Home"
                        } else {
                            Toast.makeText(this, "You are not in Bangkok.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Unable to get address for current location.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Unable to find current location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Handle the result of the location permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // This method is called when the map is ready to be used
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
}
