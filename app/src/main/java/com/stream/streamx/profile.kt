package com.stream.streamx

import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.entity.StringEntity
import org.json.JSONObject
import android.provider.Telephony
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest


class profile : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance().reference
    var payedamount: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
            getnotif()
        val backx = findViewById<ImageButton>(R.id.bctohome)
        backx.setOnClickListener {
            startActivity(Intent(applicationContext, home::class.java))
        }

        val sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userDataJson = sharedPreferences?.getString("userData", null)

            val gson = Gson()
            val userData = gson.fromJson(userDataJson, UserData::class.java)
            val balx = userData.balance
            val mailx = userData.email

        val pbal =findViewById<TextView>(R.id.probal)
        pbal.text = "Ksh "+balx+".00"
        val pemail = findViewById<TextView>(R.id.proemail)
        pemail.text = mailx

        val ut = findViewById<EditText>(R.id.urticket)
        val subtick = findViewById<Button>(R.id.subticket)

        subtick.setOnClickListener {
            // Get the entered ticket information
            val ticketInfo = ut.text.toString().trim()

            // Get user data from SharedPreferences
            val sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
            val userDataJson = sharedPreferences?.getString("userData", null)

            val gson = Gson()
            val userData = gson.fromJson(userDataJson, UserData::class.java)
            val email = userData.email

            // Create a data map to be stored in Realtime Database
            val data = hashMapOf(
                "email" to email,
                "ticket" to ticketInfo
                // Add more fields as needed
            )

            // Store data in Realtime Database
            database.child("tickets").push().setValue(data)
                .addOnSuccessListener {
                Toast.makeText(applicationContext, "ticket submitted successfully", Toast.LENGTH_LONG).show()
                ut.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(applicationContext, "ticket submitted successfully", Toast.LENGTH_LONG).show()

                }
        }


        //add payments
        val addbt = findViewById<Button>(R.id.addbtn)
        addbt.setOnClickListener {
//            showPhoneNumberDialog(this)
            showPayPopup()
        }
    }//ends oncreate


    private fun showPayPopup() {
        // Create a Dialog object
        val dialog = Dialog(this)

        // Set the content view by inflating the pay.xml layout
        dialog.setContentView(LayoutInflater.from(this).inflate(R.layout.pay, null))

        // Optional: Set other dialog properties like title, background, etc.

        // Show the dialog
        dialog.show()
        val cnum = dialog.findViewById<TextView>(R.id.copynum)
        cnum.paintFlags = cnum.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        cnum.setOnClickListener {
            copynopen(this)
        }

        val verif = dialog.findViewById<Button>(R.id.verpay)
        verif.setOnClickListener {
            checkLatestMpesaSmsForKeyword(this)
        }


    }

    fun copynopen(context: Context){
        val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("label", "0711858429")
        clipboardManager.setPrimaryClip(clipData)


        val intent = Intent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 (API level 30) and above
            intent.action = "android.telephony.euicc.action.SHOW_SIM_ABOUT"
        } else {
            // For Android versions below 11
            intent.action = "android.intent.action.MAIN"
            intent.component = ComponentName("com.android.stk", "com.android.stk.StkLauncherActivity")
        }

// Launch the SIM Toolkit app
        startActivity(intent)


        // Show a toast indicating that the text has been copied
        Toast.makeText(context, "phone number copied", Toast.LENGTH_SHORT).show()


    }//ends copy and open function




    //verify the payment
    fun checkLatestMpesaSmsForKeyword(context: Context) {
        // Define the URI for SMS messages
        val uri: Uri = Telephony.Sms.CONTENT_URI

        // Define the projection for the query
        val projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY)

        // Query for the latest SMS messages from the sender "MPESA"
        val selection = "${Telephony.Sms.ADDRESS} LIKE ?"
        val selectionArgs = arrayOf("%MPESA%")

        // Query for the latest 5 SMS messages
        val cursor = context.contentResolver.query(
            uri,  // URI for SMS content provider
            projection,  // Columns to retrieve
            selection,  // Selection criteria (filter by sender)
            selectionArgs,  // Selection arguments (sender filter)
            "${Telephony.Sms.DEFAULT_SORT_ORDER} LIMIT 5"
        )

        // Iterate through the cursor to retrieve SMS messages
        cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                // Retrieve sender address and message body
                val address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                val body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                Log.d("bodyz", "$address +++++++  $body")
                val sharedPreferencesx2 = context.getSharedPreferences("savedcode", Context.MODE_PRIVATE)
                val savedcode = sharedPreferencesx2.getString("confirmationList", "")
                // Check if the message contains the keyword "JERESON KIBURI"
                if (body.contains("JERESON", ignoreCase = true)  && savedcode?.none { body.contains(it) } == true  ) {
                    // Use regular expression to extract amount in the format "Ksh5.00"
                    val amountPattern = "Ksh(\\d+\\.?\\d*)".toRegex()
                    val matchResult = amountPattern.find(body)
                    matchResult?.let { result ->
                        val amountStr = result.groupValues[1] // Extract the amount string
                        val amount = amountStr.toDoubleOrNull()?.toInt() ?: 0
                        //Toast.makeText(context, "Amount: Ksh $amount", Toast.LENGTH_LONG).show()
                        payedamount = amount
                    }//ends match result
                    val confirmationPattern = "(\\w+) Confirmed".toRegex()
                    val confirmationMatchResult = confirmationPattern.find(body)
                    confirmationMatchResult?.let { result ->
                        val confirmationStr = result.groupValues[1] // Extract the confirmation string
                        // Save the string in shared preferences as a list
                        val sharedPreferencesx = context.getSharedPreferences("savedcode", Context.MODE_PRIVATE)
                        sharedPreferencesx.edit().putString("confirmationList", confirmationStr).apply()
                        val updatedList = sharedPreferencesx.getString("confirmationList", "")
                        if (updatedList != null) {
                        //    Toast.makeText(context, "Updated List: ${updatedList}", Toast.LENGTH_SHORT).show()
                        }
                    }//ends the confirmation

                    //update the shared preferences
                    val sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                    val userDataJson = sharedPreferences?.getString("userData", null)

                    val gson = Gson()
                    val userData = gson.fromJson(userDataJson, UserData::class.java)

                    val balx = userData.balance // Get the existing balance
                    val newBalance = balx + payedamount // Add the extracted amount to the existing balance
                    userData.balance = newBalance

                    val currentTime = Calendar.getInstance()
                    val expiryTime = Calendar.getInstance().apply {
                        add(Calendar.HOUR_OF_DAY, 24)
                    }

                    val expiryFormatted = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(expiryTime.time)

                    userData.expiry = expiryFormatted

                    val updatedUserDataJson = gson.toJson(userData)

                    sharedPreferences?.edit()?.apply {
                        putString("userData", updatedUserDataJson)
                        apply()
                        Toast.makeText(applicationContext, "expiry is: ${userData.expiry}", Toast.LENGTH_LONG).show()
                        recreate()
                    }


                    Log.d("SMS_MATCH", "Sender: $address, Body: $body")
                }//ends if
            }
        }
    }



    fun getnotif() {
        val database = FirebaseDatabase.getInstance()
        val notificationsRef = database.getReference("notifications")

        notificationsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val notification = snapshot.child("notif").getValue(String::class.java)
                // Process the notification here
                // For example, display it in a TextView
                notification?.let {
                    val tview = findViewById<TextView>(R.id.thenotif)
                    tview.text = notification
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle updated notifications if needed
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle removed notifications if needed
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle moved notifications if needed
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })
    }//ends getnotif function
//#2196F3
}//ends class