package com.stream.streamx

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class MatchAdapter(private val context: Context, private var matchList: List<MatchDetails>) :
    RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val matchLayout: LinearLayout = itemView.findViewById(R.id.matchLayout)
        val imglImageView: ImageView = itemView.findViewById(R.id.imgl)
        val team1TextView: TextView = itemView.findViewById(R.id.team1TextView)
        val team2TextView: TextView = itemView.findViewById(R.id.team2TextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val currentItem = matchList[position]

        holder.team1TextView.text = currentItem.team1
        holder.team2TextView.text = currentItem.team2
        holder.timeTextView.text = currentItem.time

        // Load online images using Glide
        Glide.with(holder.itemView.context)
            .load(currentItem.imglUrl)
            .into(holder.imglImageView)

//        Glide.with(holder.itemView.context)
//            .load(currentItem.img1Url)
//            .into(holder.img1ImageView)
//
//        Glide.with(holder.itemView.context)
//            .load(currentItem.img2Url)
//            .into(holder.img2ImageView)

       // FirebaseApp.initializeApp(context)


        holder.matchLayout.setOnClickListener {
            val sharedPreferences = context.applicationContext.getSharedPreferences("user_data", Context.MODE_PRIVATE)
            val userDataJson = sharedPreferences?.getString("userData", null)

            val gson = Gson()
            val userData = gson.fromJson(userDataJson, UserData::class.java)
            if(userData.expiry == ""){
            nosub()
            }else{
            //check for validity
                if (System.currentTimeMillis() > parseDateTimeToMillis(userData.expiry)) {
                 if(userData.balance > 9){
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
                     if(newbal > 9){
                         val databaseReference = FirebaseDatabase.getInstance().getReference("games")

                         databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                             override fun onDataChange(dataSnapshot: DataSnapshot) {
                                 var matchFound = false
                                 var link: String? = null

                                 for (childSnapshot in dataSnapshot.children) {
                                     val id = childSnapshot.child("id").getValue(String::class.java)

                                     if (id == currentItem.id) {
                                         // Match found
                                         link = childSnapshot.child("link").getValue(String::class.java)
                                         matchFound = true
                                         break
                                     }
                                 }

                                 if (matchFound) {
                                     // Start MainActivity and pass id and link as extras
                                     val intent = Intent(holder.itemView.context, MainActivity::class.java).apply {
                                         putExtra("id", currentItem.id)
                                         putExtra("link", link)
                                     }
                                     holder.itemView.context.startActivity(intent)
                                 } else {
                                     Toast.makeText(
                                         holder.itemView.context,
                                         "Match with ID ${currentItem.id} hasn't started",
                                         Toast.LENGTH_LONG
                                     ).show()
                                 }
                             }

                             override fun onCancelled(databaseError: DatabaseError) {
                                 // Handle errors here if needed
                                 Toast.makeText(
                                     holder.itemView.context,
                                     "Error accessing database: ${databaseError.message}",
                                     Toast.LENGTH_LONG
                                 ).show()
                             }
                         })
                     }else{
                         nosub()
                     }

                 }else  {nosub()}
                } else {
                    val databaseReference = FirebaseDatabase.getInstance().getReference("games")

                    databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            var matchFound = false
                            var link: String? = null

                            for (childSnapshot in dataSnapshot.children) {
                                val id = childSnapshot.child("id").getValue(String::class.java)

                                if (id == currentItem.id) {
                                    // Match found
                                    link = childSnapshot.child("link").getValue(String::class.java)
                                    matchFound = true
                                    break
                                }
                            }

                            if (matchFound) {
                                // Start MainActivity and pass id and link as extras
                                val intent = Intent(holder.itemView.context, MainActivity::class.java).apply {
                                    putExtra("id", currentItem.id)
                                    putExtra("link", link)
                                }
                                holder.itemView.context.startActivity(intent)
                            } else {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Match with ID ${currentItem.id} hasn't started",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle errors here if needed
                            Toast.makeText(
                                holder.itemView.context,
                                "Error accessing database: ${databaseError.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
                }
            }

        }//ends item on click listener

        // Set up automatic horizontal scrolling animation for each item
      //  startAutoScrollAnimation(holder.matchLayout)
    }

    //override fun getItemCount() = matchList.size
    override fun getItemCount(): Int {
        return matchList.size
    }

//    private fun startAutoScrollAnimation(matchLayout: LinearLayout) {
//        val childCount = matchLayout.childCount
//        for (i in 0 until childCount) {
//            val childView = matchLayout.getChildAt(i)
//            val animation = TranslateAnimation(-38f, 20f, 0f, 0f) // Left to right movement
//            animation.duration = 1000 // Duration of the animation in milliseconds
//            animation.repeatCount = Animation.INFINITE
//            animation.repeatMode = Animation.REVERSE
//            childView.startAnimation(animation)
//        }
//    }

    fun setData(newData: List<MatchDetails?>) {
        matchList = newData.filterNotNull() // Filter out null items
        notifyDataSetChanged()
    }

    fun nosub(){
        val dialog = Dialog(context)
        dialog.setContentView(LayoutInflater.from(context).inflate(R.layout.nosub, null))
        dialog.show()
        val gbtn = dialog.findViewById<Button>(R.id.gotoadd)
        gbtn.setOnClickListener {
            val intent = Intent(context, profile::class.java)
            context.startActivity(intent)
        }
    }

    private fun parseDateTimeToMillis(dateTimeString: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = dateFormat.parse(dateTimeString)
        return date?.time ?: 0L
    }

    fun nowplay(){

    }//ends fun nowplay

}//
