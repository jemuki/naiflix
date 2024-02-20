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
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class MovieAdapter(private val context: Context, private val moviesx: List<Moviemodel>) :
    RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {
    private var popupWindow: PopupWindow? = null

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView = itemView.findViewById(R.id.imgpre)
        val nameTextView: TextView = itemView.findViewById(R.id.namepre)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_movies, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = moviesx[position]

        // Load the image using Picasso or Glide
        if (movie.iconUrl.isNotEmpty()) {
            Picasso.get().load(movie.iconUrl).fit().placeholder(R.drawable.baseline_search_24).into(holder.iconImageView)
        } else {
            holder.iconImageView.setImageResource(R.drawable.baseline_search_24)

        }

        holder.nameTextView.text = movie.name

        holder.itemView.setOnClickListener {
            // Handle item click if needed
            showPopup(movie.trailerUrl, movie.movieLink)

            // You can use movie.trailerUrl and movie.movieLink here
        }
    }

    override fun getItemCount(): Int {
        return moviesx.size
    }


    private fun showPopup(trailerUrl: String, movieLink: String) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.movie_preview, null)

        val videoView: VideoView = popupView.findViewById(R.id.playtrailer)
        val watchNowButton: Button = popupView.findViewById(R.id.watchnow)

        // Set up VideoView with the trailer URL
        videoView.setVideoPath(trailerUrl)
        videoView.requestFocus()

        // Set up the PopupWindow
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )


        // Show the PopupWindow with a 10dp top margin
        popupWindow.showAtLocation(
            (context as Activity).findViewById(android.R.id.content),
            Gravity.CENTER,
            0,
            context.dpToPx(10)
        )

        // Play the video when the "Watch Now" button is clicked
        watchNowButton.setOnClickListener {

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
                            val intent = Intent(context, movies::class.java)
                            intent.putExtra("chosenlink", movieLink)
                            (context as Activity).startActivity(intent)
                        }else{
                            nosub()
                        }

                    }else { nosub() }
                } else {
                    val intent = Intent(context, movies::class.java)
                    intent.putExtra("chosenlink", movieLink)
                    (context as Activity).startActivity(intent)

                }
            }

        }//ends inclick listener
        videoView.start()

        // Dismiss the PopupWindow when the video finishes playing
        videoView.setOnCompletionListener {
            popupWindow.dismiss()
        }

        // Dismiss the PopupWindow when clicking outside of it
        popupView.setOnClickListener {
            popupWindow.dismiss()
        }
    }


    private fun Context.dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
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


}
