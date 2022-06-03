package com.example.safespace.models

import com.google.firebase.Timestamp
import java.util.*

class ChatMessage {
    lateinit var senderId : String
    lateinit var recieverId : String
    lateinit var message : String
    lateinit var dateTime : String
    lateinit var dateObject : Timestamp
}