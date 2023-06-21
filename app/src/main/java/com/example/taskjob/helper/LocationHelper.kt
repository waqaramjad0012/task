package com.example.taskjob.helper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat

class LocationHelper(private val context: Context) {
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    // Callback interface for receiving the location updates
    interface LocationCallback {
        fun onLocationReceived(latitude: Double, longitude: Double)
        fun onLocationError(errorMessage: String)
    }

    // Request location updates
    fun requestLocationUpdates(callback: LocationCallback) {
        if(isLocationEnabled())
        {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    // Called when the location has changed
                    callback.onLocationReceived(location.latitude, location.longitude)
                    stopLocationUpdates()
                }

                override fun onProviderDisabled(provider: String) {}

                override fun onProviderEnabled(provider: String) {}

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }

            // Check if the ACCESS_FINE_LOCATION permission is granted
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Request location updates
                locationManager?.requestSingleUpdate(LocationManager.GPS_PROVIDER,
                    locationListener as LocationListener, null)
            } else {
                callback.onLocationError("Location permission not granted")
            }
        }else{
            openLocationSettings()
        }

    }

    // Stop receiving location updates
    fun stopLocationUpdates() {
        locationListener?.let { locationManager?.removeUpdates(it) }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // Open the device's location settings
    private fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }
}
