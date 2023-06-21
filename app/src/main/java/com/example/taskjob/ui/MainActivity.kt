package com.example.taskjob.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.taskjob.R
import com.example.taskjob.adapter.UserAdapter
import com.example.taskjob.data.EndUser
import com.example.taskjob.data.User
import com.example.taskjob.databinding.ActivityMainBinding
import com.example.taskjob.dialog.LoadingDialog
import com.example.taskjob.helper.LocationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer


class MainActivity : AppCompatActivity(), UserAdapter.OnUserItemClickListener {



    private val PICK_IMAGE_REQUEST_CODE=9000
     var latitude:Double=0.0
     var longitude:Double=0.0
    private lateinit var locationHelper: LocationHelper
    lateinit var auth:FirebaseAuth
    lateinit var loading:LoadingDialog
    val REQUEST_IMAGE_CAPTURE=3000
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding:ActivityMainBinding
    lateinit var usersList :MutableList<EndUser>
    lateinit var adapter: UserAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        locationHelper= LocationHelper(this)
        loading= LoadingDialog(this)
        firestore = FirebaseFirestore.getInstance()

        auth=FirebaseAuth.getInstance()
        usersList= mutableListOf<EndUser>()
        fetchAllUsers()

        binding.addUser.setOnClickListener {
            showOptionsDialog()
        }

        getCurrentLatLongOfUser()

    }


    private fun AddNewEndUser(name: String, cnic: String, gender: String) {

                        val us= EndUser(name,cnic,gender,latitude,longitude)

        val loadingDialog=LoadingDialog(this)
        loadingDialog.show()
        firestore.collection("EndUser").document(cnic).get().addOnSuccessListener {
            if(!it.exists())
            {
                firestore.collection("EndUser").document(cnic)
                    .set(us)
                    .addOnSuccessListener {
                        loadingDialog.dismiss()
                        Toast.makeText(this@MainActivity, "User Added!!", Toast.LENGTH_SHORT).show()

                        refreshRecyclerView()
                    }
                    .addOnFailureListener { e ->
                        loadingDialog.dismiss()
                        Toast.makeText(this@MainActivity, e.localizedMessage, Toast.LENGTH_SHORT)
                            .show()

                    }
            }else{
                loadingDialog.dismiss()
                Toast.makeText(this, "user already exist", Toast.LENGTH_SHORT).show()

            }
        }

    }

    private fun EditEndUser(name: String, cnic: String, gender: String) {

        val us= EndUser(name,cnic,gender,latitude,longitude)

        val loadingDialog=LoadingDialog(this)
        loadingDialog.show()

                firestore.collection("EndUser").document(cnic)
                    .set(us)
                    .addOnSuccessListener {
                        loadingDialog.dismiss()
                        Toast.makeText(this@MainActivity, "User updated!!", Toast.LENGTH_SHORT).show()

                        refreshRecyclerView()
                    }
                    .addOnFailureListener { e ->
                        loadingDialog.dismiss()
                        Toast.makeText(this@MainActivity, e.localizedMessage, Toast.LENGTH_SHORT)
                            .show()

                    }



    }
    private fun refreshRecyclerView() {
        firestore.collection("EndUser")
            .get()
            .addOnSuccessListener { querySnapshot ->

                usersList= mutableListOf()
                for (document in querySnapshot) {
                    val user = document.toObject(EndUser::class.java)
                    usersList.add(user)
                }

                adapter.refreshList(usersList)

            }
            .addOnFailureListener { e ->
                // Handle any errors that occurred while fetching the users
            }
    }

    private fun fetchAllUsers() {
        firestore.collection("EndUser")
            .get()
            .addOnSuccessListener { querySnapshot ->

                usersList= mutableListOf()
                for (document in querySnapshot) {
                    val user = document.toObject(EndUser::class.java)
                    usersList.add(user)
                }

                adapter= UserAdapter(usersList ,this)
                binding.recyclerUsers.adapter=adapter

            }
            .addOnFailureListener { e ->
                // Handle any errors that occurred while fetching the users
            }
    }


    override fun onEditButtonClick(user: EndUser) {
        Toast.makeText(this, "${user.name}", Toast.LENGTH_SHORT).show()
        showEditDialog(user)
    }

    override fun onDeleteButtonClick(user: EndUser) {
        Toast.makeText(this, "Do you really want to delete?", Toast.LENGTH_SHORT).show()






        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Warning")
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setMessage("Are you sure you want to delete this item?")

        alertDialogBuilder.setPositiveButton("Delete") { dialog: DialogInterface, _: Int ->
            val documentRef = user.cnic?.let { firestore.collection("EndUser").document(it) }
            if (documentRef != null) {
                documentRef.delete()
                    .addOnSuccessListener {
                        // Item deleted successfully
                        refreshRecyclerView()
                        Toast.makeText(this, "User Deleted..", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "exception $exception", Toast.LENGTH_SHORT).show()
                    }
            }
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }

    override fun onUserItemClick(user: EndUser) {
        val intent=Intent(this,ViewDetails::class.java)
        intent.putExtra("name",user.name)
        intent.putExtra("cnic",user.cnic)
        intent.putExtra("gender",user.gender)
        intent.putExtra("latitude",user.lat)
        intent.putExtra("longitude",user.longitude)

        startActivity(intent)
    }

    private fun showOptionsDialog() {
        val options = arrayOf("Fill Form Automatically", "Fill Form Manually")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select an option")
        builder.setCancelable(false)
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {

                    showImageSourceDialog()
                    // Option 1: Fill Form from Picture
                    // Implement the logic to fill the form automatically from a picture
                    dialog.dismiss()
                }
                1 -> {
                   showSignupDialog("","","")
                    dialog.dismiss()
                }
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select an image source")
        builder.setCancelable(false)
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    dispatchTakePictureIntent()
                    dialog.dismiss()
                }
                1 -> {
                    // Option 1 index: Choose from Gallery
                    // Implement the logic to select a photo from the gallery
                    openGallery()
                    dialog.dismiss()
                }
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }
    fun extractTextFromImage(imageBitmap: Bitmap): String {
        var extrText=""
        val image = FirebaseVisionImage.fromBitmap(imageBitmap)
        val recognizer: FirebaseVisionTextRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        recognizer.processImage(image).addOnSuccessListener {
            extrText= it.text

            Toast.makeText(this, "$extrText", Toast.LENGTH_SHORT).show()
        }
            .addOnFailureListener {
                extrText=it.localizedMessage
            }
        return extrText
    }

    private fun showToast(resultText: String) {
        Toast.makeText(this, "$resultText", Toast.LENGTH_SHORT).show()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // The image capture was successful, you can process the captured image here
            val imageBitmap = data?.extras?.get("data") as Bitmap
            // Do something with the imageBitmap

//            extractTextFromImage(imageBitmap)
            processImage(imageBitmap)
        }
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
          val bmp=  convertUriToBitmap(selectedImageUri)
            if(bmp!=null)
            {
                processImage(bmp)
            }
        }
    }


    private fun convertUriToBitmap(uri: Uri?): Bitmap? {
        try {
            if(uri!=null)
            {
            val inputStream = contentResolver.openInputStream(uri)
            return BitmapFactory.decodeStream(inputStream)
            }else{
                return null;
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    private fun extractDataFromText(visionText: FirebaseVisionText) {

        loading.setMessage("Extracting data from image")
        val blocks = visionText.textBlocks

        var name = ""
        var gender = ""
        var cnicNumber = ""

        var previous=""
        for (block in blocks) {
            Log.d("TAGGG", "extractDataFromText: ${blocks.size}")
            for (line in block.lines) {
                val text = line.text.toLowerCase()
                if(previous.contains("name",true)&& !previous.contains("father",true))
                {
                    name=text;
                }else if(previous.contains("gender",true))
                {
                    gender=text
                }else if(previous.contains("identity number",true))
                {
                    cnicNumber=text;
                }
                Log.d("TAGGG", "extractDataFromText: $text" )
               previous=text;
            }

            loading.dismiss()
            showSignupDialog(name,cnicNumber,gender)

        }



    }


    private fun showSignupDialog( nm:String, cnic:String, gender:String) {


        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_signup, null)

        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val cnicEditText = dialogView.findViewById<EditText>(R.id.cnicEditText)
        val genderRadioGroup = dialogView.findViewById<RadioGroup>(R.id.genderRadioGroup)
        val maleRadioButton = dialogView.findViewById<RadioButton>(R.id.maleRadioButton)
        val femaleRadioButton = dialogView.findViewById<RadioButton>(R.id.femaleRadioButton)

       val gnderZeroIndex=gender.split(" ")[0]
        if(gnderZeroIndex.contains("m",true))
        {
            maleRadioButton.isChecked=true
        }else if(gnderZeroIndex.contains("m",true))
        {
            femaleRadioButton.isChecked=true
        }
         nameEditText.setText(nm)
         cnicEditText.setText(cnic)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .setTitle("Add New User")
            .setPositiveButton("Add") { dialog, which ->
                val name = nameEditText.text.toString()
                val cnic = cnicEditText.text.toString()
                val genderId = genderRadioGroup.checkedRadioButtonId
                val genderRadioButton = dialogView.findViewById<RadioButton>(genderId)
                val gender = genderRadioButton.text.toString()

                if(latitude==0.0)
                {
                    getCurrentLatLongOfUser()
                }
                AddNewEndUser(name,cnic,gender)
                // Perform signup process with the entered data
                // ...
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }


    private fun showEditDialog( us:EndUser) {


        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val cnicInfo = dialogView.findViewById<TextView>(R.id.cnicEditText)
        val genderRadioGroup = dialogView.findViewById<RadioGroup>(R.id.genderRadioGroup)
        val maleRadioButton = dialogView.findViewById<RadioButton>(R.id.maleRadioButton)
        val femaleRadioButton = dialogView.findViewById<RadioButton>(R.id.femaleRadioButton)

        val gnderZeroIndex=us.gender!!.split(" ")[0]
        if(gnderZeroIndex.contains("m",true))
        {
            maleRadioButton.isChecked=true
        }else if(gnderZeroIndex.contains("m",true))
        {
            femaleRadioButton.isChecked=true
        }
        nameEditText.setText(us.name)
        cnicInfo.setText(us.cnic)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .setTitle("Edit User")
            .setPositiveButton("Edit") { dialog, which ->
                val name = nameEditText.text.toString()
                val cnic = cnicInfo.text.toString()
                val genderId = genderRadioGroup.checkedRadioButtonId
                val genderRadioButton = dialogView.findViewById<RadioButton>(genderId)
                val gender = genderRadioButton.text.toString()

                if(latitude==0.0)
                {
                    getCurrentLatLongOfUser()
                }
                EditEndUser(name,cnic,gender)
                // Perform signup process with the entered data
                // ...
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }
    private fun processImage(imageBitmap: Bitmap) {
        loading.setMessage("please wiat...")
        loading.show()

        val image = FirebaseVisionImage.fromBitmap(imageBitmap)
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

        detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                extractDataFromText(firebaseVisionText)
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error processing image: ${exception.message}")
            }
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
                this@MainActivity.latitude =latitude
                this@MainActivity.longitude=longitude

                Toast.makeText(this@MainActivity, "$latitude , $longitude", Toast.LENGTH_SHORT).show()
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
