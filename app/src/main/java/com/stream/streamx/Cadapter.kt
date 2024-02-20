package com.stream.streamx

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class Cadapter(private val context: Context, private val channelList: List<ChannelModel>) :
    RecyclerView.Adapter<Cadapter.ChannelViewHolder>() {
    private var popupWindow: PopupWindow? = null

    class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView = itemView.findViewById(R.id.tviconx)
        val nameTextView: TextView = itemView.findViewById(R.id.tvnamex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channelList[position]

        // Load the image using Picasso or Glide
        if (channel.cicon != "") {
            Picasso.get().load(channel.cicon).fit().placeholder(R.drawable.baseline_live_tv_24)
                .into(holder.iconImageView)
        } else {
            holder.iconImageView.setImageResource(R.drawable.baseline_live_tv_24)
        }

        holder.nameTextView.text = channel.cname

        holder.itemView.setOnClickListener {

            val sharedPreferences = context.applicationContext.getSharedPreferences("user_data", Context.MODE_PRIVATE)
            val userDataJson = sharedPreferences?.getString("userData", null)

            val gson = Gson()
            val userData = gson.fromJson(userDataJson, UserData::class.java)
            if(userData.expiry == ""){
                nosub()
            }else{
                //check for validity
                if (System.currentTimeMillis() > parseDateTimeToMillis(userData.expiry)) {
                    if (userData.balance > 9){
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
                            val intent = Intent(context, watchtv::class.java)
                            intent.putExtra("channelLink", channel.clink)
                            context.startActivity(intent)
                        }else{
                            nosub()
                        }

                    }else { nosub() }
                } else {
                    val intent = Intent(context, watchtv::class.java)
                    intent.putExtra("channelLink", channel.clink)
                    context.startActivity(intent)

                }
            }


        }//ends on click listener
    }

    override fun getItemCount(): Int {
        return channelList.size
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


    fun nowwatch(){
//        val intent = Intent(context, watchtv::class.java)
//        intent.putExtra("channelLink", channel.clink)
//        context.startActivity(intent)

    }


}
