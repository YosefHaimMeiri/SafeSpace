package com.example.safespace.models

import java.io.Serializable
import java.util.*

class User : Serializable {
   lateinit var name : String
   lateinit var email : String
   lateinit var image : String
   lateinit var token : String
   lateinit var id : String
   lateinit var dateObject : Date
}