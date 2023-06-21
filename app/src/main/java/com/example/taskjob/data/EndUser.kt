package com.example.taskjob.data

import java.io.Serializable

data class EndUser ( var name:String?=null, var cnic:String?=null, val gender: String?=null, val lat:Double?=null, val longitude:Double?=null)
    : Serializable