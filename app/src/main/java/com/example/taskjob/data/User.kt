package com.example.taskjob.data

import java.io.Serializable

data class User( var name:String?=null, var id:String?=null,
                 val email: String?=null, var password: String?=null,
                 val lat:Double?=null, val longitude:Double?=null):Serializable
