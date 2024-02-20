package com.stream.streamx

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsMessage
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import com.google.gson.Gson

class phone : AppCompatActivity() {

    private lateinit var enterPhoneEditText: EditText
    private lateinit var submitPhoneButton: Button
    private lateinit var progre: ProgressBar
    private lateinit var databaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)

        progre = findViewById(R.id.propho)
        progre.visibility = View.GONE
        enterPhoneEditText = findViewById(R.id.enterphone)
        submitPhoneButton = findViewById(R.id.verifybtn)

        databaseReference = FirebaseDatabase.getInstance().getReference("naiflix")

        submitPhoneButton.setOnClickListener {
            requestSmsPermission()
        }
    }//ends oncreate
    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_SMS),
                SMS_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is already granted, proceed
            uploadPhone()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed
                uploadPhone()
            } else {
                requestSmsPermission()
                // For simplicity, let's show a toast message
                //  showToast("SMS permission denied. You can't proceed.")
            }
        }
    }


    companion object {
        private const val SMS_PERMISSION_REQUEST_CODE = 123
    }

    private fun uploadPhone() {
        progre.visibility = View.VISIBLE
        val phoneNumber = enterPhoneEditText.text.toString()

        // Retrieve user data from SharedPreferences
        val sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        val userDataJson = sharedPreferences.getString("userData", null)

        if (userDataJson != null) {
            // Convert JSON to UserData object
            val gson = Gson()
            val userData = gson.fromJson(userDataJson, UserData::class.java)

            // Upload phone number to Firebase
            uploadPhoneToFirebase(userData.email, phoneNumber)
        } else {
            // Handle the case where user data is not available in SharedPreferences
            // You may want to redirect the user to the login page or handle this situation accordingly
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun uploadPhoneToFirebase(email: String, phoneNumber: String) {
        // Check if the email exists in Firebase
        databaseReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Email exists, update the phone number
                        dataSnapshot.children.first().ref.child("phone").setValue(phoneNumber)
                        progre.visibility = View.GONE
                        startActivity(Intent(applicationContext, home::class.java))
                    } else {
                        // Handle the case where email doesn't exist in Firebase
                        // This should not happen if the user has logged in successfully
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    // For simplicity, print the error message
                    println("Database Error: ${databaseError.message}")
                }
            })
    }
}
