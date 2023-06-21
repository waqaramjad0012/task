package com.example.taskjob.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.taskjob.data.User
import com.example.taskjob.databinding.ActivitySignUpBinding
import com.example.taskjob.helper.LocationHelper
import com.example.taskjob.viewModel.SignUpViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var locationHelper: LocationHelper
    lateinit var viewModel: SignUpViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = SignUpViewModel()
        binding.signupdata = viewModel
        binding.lifecycleOwner = this

        locationHelper= LocationHelper(this)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.signup.setOnClickListener {

            signUpUser(viewModel.email.value!!,viewModel.password.value!!,viewModel.name.value!!,viewModel.id.value!!)
        }
        binding.signIn.setOnClickListener {
            startActivity(Intent(this,LoginScreenActivity::class.java))
            finishAffinity()
        }

    }

    private fun signUpUser(email: String, password: String, name: String, id: String) {

        Toast.makeText(this, "$email, $password", Toast.LENGTH_SHORT).show()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user != null) {
                        val userData = HashMap<String, Any>()
                        userData["email"] = email
                        userData["name"] = name
                        userData["id"] = id

                        val us= User(name,id,email,password,viewModel.lat,viewModel.longi)
                        firestore.collection("users").document(id)
                            .set(us)
                            .addOnSuccessListener {
                                Toast.makeText(this@SignUp, "User SignUp!!", Toast.LENGTH_SHORT).show()
                                gotoLogin()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@SignUp, e.localizedMessage, Toast.LENGTH_SHORT)
                                    .show()
                            }


//                        firestore.collection("users")
//                            .document(user.uid)
//                            .set(userData)
//                            .addOnSuccessListener {
//                                Toast.makeText(
//                                    this,
//                                    "User created successfully",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                                // Perform any further actions after successful sign-up
//                            }
//                            .addOnFailureListener { e ->
//                                Toast.makeText(
//                                    this,
//                                    "Error creating User: ${e.message}",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Failed to create user: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun gotoLogin() {
      startActivity(Intent(this@SignUp,LoginScreenActivity::class.java))
        finishAffinity()
    }

    override fun onResume() {
        super.onResume()

        getCurrentLatLongOfUser()
    }
    fun getCurrentLatLongOfUser(){

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        } else {
            getLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        // Request location updates
        locationHelper.requestLocationUpdates(object : LocationHelper.LocationCallback {
            override fun onLocationReceived(latitude: Double, longitude: Double) {
                // Handle the received latitude and longitude
                // Here, you can perform any actions or logic with the received location data
                viewModel.lat=latitude
                viewModel.longi=longitude

                Toast.makeText(this@SignUp, "$latitude , $longitude", Toast.LENGTH_SHORT).show()
            }

            override fun onLocationError(errorMessage: String) {
                // Handle the location error
                println("Location error: $errorMessage")
            }
        })


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHelper.stopLocationUpdates()
    }
}