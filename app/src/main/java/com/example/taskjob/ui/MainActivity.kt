package com.example.taskjob.ui


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.taskjob.R
import com.example.taskjob.adapter.UserAdapter
import com.example.taskjob.data.User
import com.example.taskjob.databinding.ActivityMainBinding
import com.example.taskjob.dialog.LoadingDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer


class MainActivity : AppCompatActivity(), UserAdapter.OnUserItemClickListener {

    lateinit var auth:FirebaseAuth
    lateinit var loading:LoadingDialog
    val REQUEST_IMAGE_CAPTURE=3000
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding:ActivityMainBinding
    lateinit var usersList :MutableList<User>
    lateinit var adapter: UserAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loading= LoadingDialog(this)
        firestore = FirebaseFirestore.getInstance()

        auth=FirebaseAuth.getInstance()
        usersList= mutableListOf<User>()
        fetchAllUsers()

        binding.addUser.setOnClickListener {
            showOptionsDialog()
        }

    }


    private fun signUpUser(email: String, password: String, name: String, id: String) {

        Toast.makeText(this, "$email, $password", Toast.LENGTH_SHORT).show()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user != null) {


                        val us= User(name,id,email,password,viewModel.lat,viewModel.longi)
                        firestore.collection("users").document(id)
                            .set(us)
                            .addOnSuccessListener {
                                Toast.makeText(this@MainActivity, "User SignUp!!", Toast.LENGTH_SHORT).show()
                                gotoLogin()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@MainActivity, e.localizedMessage, Toast.LENGTH_SHORT)
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
    private fun fetchAllUsers() {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { querySnapshot ->

                usersList= mutableListOf()
                for (document in querySnapshot) {
                    val user = document.toObject(User::class.java)
                    usersList.add(user)
                }

                adapter= UserAdapter(usersList ,this)
                binding.recyclerUsers.adapter=adapter

            }
            .addOnFailureListener { e ->
                // Handle any errors that occurred while fetching the users
            }
    }


    override fun onEditButtonClick(user: User) {
        Toast.makeText(this, "${user.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onDeleteButtonClick(user: User) {
        Toast.makeText(this, "Do you really want to delete?", Toast.LENGTH_SHORT).show()
    }

    override fun onUserItemClick(user: User) {
        val intent=Intent(this,ViewDetails::class.java)
        intent.putExtra("name",user.name)
        intent.putExtra("cnic",user.email)
        intent.putExtra("gender",user.id)
        intent.putExtra("latitude",user.lat)
        intent.putExtra("longitude",user.longitude)

        startActivity(intent)
    }

    private fun showOptionsDialog() {
        val options = arrayOf("Fill Form Automatically", "Fill Form Manually")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select an option")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {

                    showImageSourceDialog()
                    // Option 1: Fill Form from Picture
                    // Implement the logic to fill the form automatically from a picture
                    dialog.dismiss()
                }
                1 -> {
                    Toast.makeText(this, "fill the form manually..", Toast.LENGTH_SHORT).show()
                    // Option 2: Fill Form Manually
                    // Implement the logic to fill the form manually
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
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    dispatchTakePictureIntent()
                    // Option 1: Take Photo
                    // Implement the logic to capture a photo using the camera


                    dialog.dismiss()
                }
                1 -> {
                    // Option 2: Choose from Gallery
                    // Implement the logic to select a photo from the gallery
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // The image capture was successful, you can process the captured image here
            val imageBitmap = data?.extras?.get("data") as Bitmap
            // Do something with the imageBitmap

//            extractTextFromImage(imageBitmap)
            processImage(imageBitmap)
        }
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
            .setTitle("Add New User")
            .setPositiveButton("Add") { dialog, which ->
                val name = nameEditText.text.toString()
                val cnic = cnicEditText.text.toString()
                val genderId = genderRadioGroup.checkedRadioButtonId
                val genderRadioButton = dialogView.findViewById<RadioButton>(genderId)
                val gender = genderRadioButton.text.toString()

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
        val icon = BitmapFactory.decodeResource(
            getResources(),
            com.example.taskjob.R.drawable.cnic
        )
        val image = FirebaseVisionImage.fromBitmap(icon)
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

        detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                extractDataFromText(firebaseVisionText)
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error processing image: ${exception.message}")
            }
    }
}
