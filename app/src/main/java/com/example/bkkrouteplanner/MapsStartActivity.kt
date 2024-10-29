package com.example.bkkrouteplanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import android.util.Log
import java.util.Locale

class MapsStartActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsstartBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var startPlaceID: String? = null
    private var currentLatLng: LatLng? = null


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(this)

        binding = ActivityMapsstartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS)
                val request = FetchPlaceRequest.builder(place.id!!, placeFields).build()

                placesClient.fetchPlace(request).addOnSuccessListener { response ->
                    val placeDetail = response.place
                    val addressComponents = placeDetail.addressComponents?.asList()
                    var isInBangkok = false

                    addressComponents?.forEach { component ->
                        if (component.types.contains("postal_code")) {
                            val postalCode = component.name.toIntOrNull()
                            if (postalCode != null && postalCode in 10000..10999) {
                                isInBangkok = true
                            }
                        }
                    }

                    if (isInBangkok) {
                        val latLng = placeDetail.latLng
                        if (latLng != null) {
                            startPlaceID = placeDetail.id
                            mMap.addMarker(MarkerOptions().position(latLng).title(placeDetail.name))
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
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
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            val resultIntent = Intent()

            if (!startPlaceID.isNullOrEmpty()) {
                resultIntent.putExtra("START_PLACE_ID", startPlaceID)

            } else {
                currentLatLng?.let {
                    val latLngString = "${it.latitude},${it.longitude}"
                    resultIntent.putExtra("START_PLACE_LL", latLngString)
                }
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        val currentLocation = findViewById<Button>(R.id.currentLocationButton)
        currentLocation.setOnClickListener {
            getCurrentLocation()
        }

    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLatLng = LatLng(location.latitude, location.longitude)

                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    if (addressList != null && addressList.isNotEmpty()) {
                        val postalCode = addressList[0].postalCode
                        if (postalCode != null && postalCode.toInt() in 10000..10999) {
                            mMap.addMarker(MarkerOptions().position(currentLatLng!!).title("Current Location"))
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng!!, 15f))
                            Log.d("currentLocation", "Current location is in Bangkok: ${currentLatLng}")
                        } else {
                            Toast.makeText(this, "Current location is not in Bangkok", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Unable to get address from location", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
}
