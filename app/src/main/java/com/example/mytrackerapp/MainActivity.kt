package com.example.mytrackerapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*

const val EXTRA_MESSAGE = "com.example.mytrackerapp.MESSAGE"
const val REQUESTING_LOCATION_UPDATES_KEY = "com.example.mytrackerapp.LOCATION"

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var requestingLocationUpdates: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                p0 ?: return
                for (location in p0.locations) {
                    Log.e("locationCallback", location.toString())
                    setText(location.toString())
                    sendMessage(location)
                }
            }
        }
        locationRequest = createLocationRequest()!!
        updateValuesFromBundle(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onResume() {
        super.onResume()
        Log.e("onResume", "${locationRequest.toString()}")
        if (!requestingLocationUpdates) {
            requestingLocationUpdates = true
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        if (requestingLocationUpdates) {
            requestingLocationUpdates = false
            stopLocationUpdates()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates)
        super.onSaveInstanceState(outState)
    }

    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        savedInstanceState ?: return
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY)
        }
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        checkPermission()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest(): LocationRequest? {
        return LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun sendMessage(location: Location) {
        val intent = Intent(this, MapsActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, location)
        }
        startActivity(intent)
    }

    private fun getLastKnownLocation() {
        checkPermission()
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            if (location != null) {
                Log.e("Location", location.toString())
                sendDweet(location)
            } else {
                Log.e("ERROR", "Cannot get location!")
            }
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ERROR", "Permission not granted")
        }
    }

    private fun sendDweet(location: Location) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://dweet.io/dweet/for/square-trouble?latitude=${location.latitude}&longitude=${location.longitude}"
        val stringRequest = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                Log.e("RESPONSE", "Response is: ${response.toString()}")
                sendMessage(location)
            }, Response.ErrorListener {
                Log.e("ERROR", "HTTP request error")
            }
        )
        queue.add(stringRequest)
    }

    private fun setText(text: String) {
        val textView = findViewById<TextView>(R.id.textView)
        textView.text = text
    }
}
