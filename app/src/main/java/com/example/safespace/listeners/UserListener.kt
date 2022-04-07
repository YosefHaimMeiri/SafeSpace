package com.example.safespace.listeners

import com.example.safespace.models.User

interface UserListener {
    fun onUserClicked(user:User)
}