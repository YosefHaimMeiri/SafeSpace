package com.example.safespace.utilities

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.util.HashMap
import kotlin.coroutines.coroutineContext

class LocationProvider(var mFusedLocationClient : FusedLocationProviderClient) {
    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private var TAG = "LocationManager"
    private lateinit var lpContext : Context
    /**
     * Represents a geographical location.
     */
    protected var mLastLocation: Location? = null
    private var mLatitude: Double? = null
    private var mLongitude: Double? = null


    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    fun UpdateLocationAndNotify()
    {
        getLastLocation()
        val database : FirebaseFirestore = FirebaseFirestore.getInstance()
        val locationHashMap : HashMap<String, Any> = HashMap()
        val north = mLatitude
        val east = mLongitude
        locationHashMap["latitude"] = north as Double
        locationHashMap["longitude"] = east as Double
        locationHashMap["time"]= LocalDateTime.now()
        database.collection("Alert_places").add(locationHashMap)
    }


    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationClient!!.lastLocation.addOnCompleteListener { task : Task<Location> ->
            if (task.isSuccessful && task.result != null) {
                mLastLocation = task.result
                mLatitude = mLastLocation!!.latitude
                mLongitude = mLastLocation!!.longitude
            } else {
                Log.w(TAG, "getLastLocation:exception", task.exception)
            }
        }
    }



}