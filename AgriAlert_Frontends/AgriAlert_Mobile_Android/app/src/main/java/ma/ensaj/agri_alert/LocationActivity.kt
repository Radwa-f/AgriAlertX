package ma.ensaj.agri_alert

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensaj.agri_alert.databinding.ActivityLocationBinding
import ma.ensaj.agri_alert.model.LocationUpdateRequest
import ma.ensaj.agri_alert.network.RetrofitClient
import ma.ensaj.agri_alert.util.SharedPreferencesHelper

class LocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityLocationBinding
    private lateinit var currentLocation: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this, R.color.my_dark)

        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /*binding.btnCurrentLocation.setOnClickListener {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
                return@setOnClickListener
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                    mMap.clear()
                    mMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                }
            }
        }*/

        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val searchText = binding.etSearch.text.toString()
            if (searchText.isNotEmpty()) {
                lifecycleScope.launch {
                    val geocoder = Geocoder(this@LocationActivity)
                    val addressList = geocoder.getFromLocationName(searchText, 1)
                    if (addressList != null) {
                        if (addressList.isNotEmpty()) {
                            val address = addressList?.get(0)
                            if (address != null) {
                                currentLocation = LatLng(address.latitude, address.longitude)
                            }
                            mMap.clear()
                            mMap.addMarker(MarkerOptions().position(currentLocation).title("Search Result"))
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                        }
                    }
                }
            }
            true
        }

        binding.btnSaveLocation.setOnClickListener {
            val token = SharedPreferencesHelper.getToken(this)
            if (!token.isNullOrEmpty()) {
                val locationRequest = LocationUpdateRequest(currentLocation.latitude, currentLocation.longitude)
                lifecycleScope.launch {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.instance.updateLocation("Bearer $token", locationRequest)
                    }
                    if (response.isSuccessful) {
                        Toast.makeText(this@LocationActivity, "Location updated successfully!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LocationActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LocationActivity, "Failed to update location.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Authentication token is missing", Toast.LENGTH_SHORT).show()
            }
        }


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set initial location
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        currentLocation = LatLng(latitude, longitude)

        val marker = mMap.addMarker(MarkerOptions().position(currentLocation).title("Selected Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))

        // Allow the marker to move
        mMap.setOnMapClickListener { newLocation ->
            currentLocation = newLocation
            marker?.position = newLocation
        }
    }


}