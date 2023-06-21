package com.example.taskjob.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.taskjob.R
import com.example.taskjob.databinding.ActivityForgotPasswordBinding
import com.example.taskjob.databinding.ActivityLoginScreenBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {
    lateinit var auth:FirebaseAuth
    lateinit var binding:ActivityForgotPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        auth= FirebaseAuth.getInstance()

        binding.resetButton.setOnClickListener {
        val email=binding.emailEditText.text.toString()
            if(email!=null && !email.isEmpty())
            {
                if(isValidEmail(email)!=null && isValidEmail(email)==true)
                {
                    resetPassword(email)
                }else{
                    Toast.makeText(this, "not valid email", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "email null or empty", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun isValidEmail(email: String?): Boolean? {
        val regexPattern = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return email?.let { regexPattern.matches(it) }

    }
    fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Password reset email sent successfully
                    Toast.makeText(this@ForgotPassword, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@ForgotPassword,LoginScreenActivity::class.java))
                    finishAffinity()
                } else {
                    // Password reset email sending failed
                    Toast.makeText(this@ForgotPassword, "Failed to send password reset email.", Toast.LENGTH_SHORT).show()
                }
            }
    }

}