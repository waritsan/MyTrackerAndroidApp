package com.example.mytrackerapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                Log.e("LOCATION", location.toString())
                Log.e("LOCATION", "Latitude: ${latitude}, longitude: ${longitude}")
                // Send request
                val queue = Volley.newRequestQueue(this)
                val url = "https://dweet.io/dweet/for/square-trouble?latitude=${latitude}&longitude=${longitude}"
                val stringRequest = StringRequest(Request.Method.GET, url, Response.Listener<String> {
                        response -> Log.e("RESPONSE", "Response is: ${response.toString()}")
                }, Response.ErrorListener { Log.e("ERROR", "HTTP request error") })
                queue.add(stringRequest)
            } else {
                Log.e("ERROR", "Cannot get location!")
            }
        }

        // Register the listener with the Location Manager to receive location updates
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Log.e("ERROR", "Permission not granted")
        }
    }
}
