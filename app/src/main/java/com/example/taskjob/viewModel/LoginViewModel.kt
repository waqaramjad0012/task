package com.example.taskjob.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
   public val email = MutableLiveData<String>()
  public  val password = MutableLiveData<String>()

    fun onLoginClicked() {
        // Perform login logic here
        val enteredEmail = email.value
        val enteredPassword = password.value

        // Validate email and password
        if (isValidEmail(enteredEmail)!=null && isValidEmail(enteredEmail) == true && isValidPassword(enteredPassword)) {
            // Login successful
        } else {
            // Login failed
        }
    }



    private fun isValidEmail(email: String?): Boolean? {
        val regexPattern = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return email?.let { regexPattern.matches(it) }

    }

    private fun isValidPassword(password: String?): Boolean {
        // Perform password validation logic here
        // Return true if the password is valid, otherwise false
        return false
    }
}
