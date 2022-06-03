package com.example.safespace.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.safespace.databinding.ActivityMapMenuBinding

class MapMenuActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMapMenuBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setListeners()
    {
        binding.imageBack.setOnClickListener { onBackPressed() }
    }


}