package com.example.taskjob.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpViewModel : ViewModel() {

    val id = MutableLiveData<String>()
    val name = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    var lat:Double=0.0
    var longi:Double=0.0


    private fun isValidEmail(email: String?): Boolean {
        // Perform email validation logic here
        // Return true if the email is valid, otherwise false

        return false
    }

    private fun isValidPassword(password: String?): Boolean {
        // Perform password validation logic here
        // Return true if the password is valid, otherwise false
        return false
    }
}
