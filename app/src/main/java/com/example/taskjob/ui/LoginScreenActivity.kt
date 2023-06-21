package com.example.taskjob.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.taskjob.databinding.ActivityLoginScreenBinding
import com.example.taskjob.viewModel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class LoginScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginScreenBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    @SuppressLint("ClickableViewAccessibility", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

      val  viewModel = LoginViewModel()
        binding.logindata = viewModel
        binding.lifecycleOwner = this

        auth=FirebaseAuth.getInstance()


        binding.btnLogin.setOnClickListener {

            if(viewModel.email.value!=null && !viewModel.email.value!!.isEmpty() && viewModel.password.value!=null && !viewModel.password.value!!.isEmpty())
            signInWithEmail(viewModel.email.value!!,viewModel.password.value!!)
        }
        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@LoginScreenActivity,MainActivity::class.java))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login ")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()


        binding.fingerPrint.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        binding.signUp.setOnClickListener {
            startActivity(Intent(this,SignUp::class.java))
            finishAffinity()
        }

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this,ForgotPassword::class.java))
        }

    }

    fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    // Proceed to the next screen or perform any other actions
                    startActivity(Intent(this,MainActivity::class.java))
                    finishAffinity()
                } else {
                    // Sign-in failed, display an error message
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }


}

