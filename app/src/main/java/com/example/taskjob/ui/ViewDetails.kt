package com.example.taskjob.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.taskjob.R
import com.example.taskjob.databinding.ActivityViewDetailsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class ViewDetails : AppCompatActivity() {
    var name:String?=""
    var cnic:String?=""
    var gender:String?=""
    private lateinit var binding:ActivityViewDetailsBinding
    private lateinit var googleMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityViewDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent!=null)
        {
             name = intent.getStringExtra("name")
             cnic = intent.getStringExtra("cnic")
             gender = intent.getStringExtra("gender")
        }

        binding.nameTextView.text="name $name"
        binding.cnicTextView.text="cnic $cnic"
        binding.genderTextView.text="gender $gender"

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { map ->
            googleMap = map
            // Add a marker for the user's location
            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)
            val userLocation = LatLng(latitude, longitude)
            googleMap.addMarker(MarkerOptions().position(userLocation))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
        }

    }
}