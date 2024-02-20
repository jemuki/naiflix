package com.stream.streamx

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed(
            Runnable {
            checkUserDataExistence()
            }, 3000
        )
    }//ends on create

    private fun checkUserDataExistence() {
        val sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        val userDataJson = sharedPreferences.getString("userData", null)

        if (userDataJson != null) {
         startActivity(Intent(applicationContext, home::class.java))
            println("User data exists: $userDataJson")
            finish()
        } else {
            startActivity(Intent(applicationContext, login::class.java))
            println("User data doesn't exist")
        }
    }
}//ends class