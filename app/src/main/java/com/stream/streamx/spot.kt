package com.stream.streamx


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*


class spot : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot)


        val sharedPreferences = applicationContext.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userDataJson = sharedPreferences?.getString("userData", null)

        val gson = Gson()
        val userData = gson.fromJson(userDataJson, UserData::class.java)
        if(userData.expiry == ""){
            nosub()
        }else{
            //check for validity
            if (System.currentTimeMillis() > parseDateTimeToMillis(userData.expiry)) {
                val newbal = userData.balance - 10
                val currentTime = Calendar.getInstance()
                val expiryTime = Calendar.getInstance().apply {
                    add(Calendar.HOUR_OF_DAY, 24)
                }

                val expiryFormatted = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(expiryTime.time)

                userData.expiry = expiryFormatted
                userData.balance = newbal
                val updatedUserDataJson = gson.toJson(userData)

                sharedPreferences?.edit()?.apply {
                    putString("userData", updatedUserDataJson)
                    apply()
                }
                val message = "new: ${userData.balance} new exp: ${userData.expiry}"
                Toast.makeText(applicationContext, "$message", Toast.LENGTH_LONG).show()
                if(newbal > 9){

                }else{
                    nosub()
                }

            } else {
                Toast.makeText(applicationContext, "Your subscription is still valid until ${userData.expiry}", Toast.LENGTH_LONG).show()

            }
        }




        val urlToOpen = "https://www.spotify.com"

        // Create a CustomTabsIntent.Builder
        val builder = CustomTabsIntent.Builder()

        // Customize the toolbar color
        builder.setToolbarColor(resources.getColor(R.color.purple_500))

        // Create a CustomTabsIntent
        val customTabsIntent = builder.build()

        // Launch the Custom Chrome Tab
        customTabsIntent.launchUrl(this, Uri.parse(urlToOpen))

    }//ends oncreate


    fun nosub(){
        val dialog = Dialog(applicationContext)
        dialog.setContentView(LayoutInflater.from(applicationContext).inflate(R.layout.nosub, null))
        dialog.show()
        val gbtn = dialog.findViewById<Button>(R.id.gotoadd)
        gbtn.setOnClickListener {
            val intent = Intent(applicationContext, profile::class.java)
            application.startActivity(intent)
        }
    }

    private fun parseDateTimeToMillis(dateTimeString: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = dateFormat.parse(dateTimeString)
        return date?.time ?: 0L
    }

}