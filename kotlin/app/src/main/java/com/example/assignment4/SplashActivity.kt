package com.example.assignment4

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen) // Ensure this layout exists and is correct

        Handler().postDelayed({
            val intent = Intent(this, Stocks::class.java)
            startActivity(intent)
            finish() // This ensures the splash activity is removed from the activity stack
        }, 3000) // Delays the transition to the main activity by 3 seconds
    }
}
