package com.stream.streamx

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class smovies : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var movieAdapter: MovieAdapter
    private var movieList: MutableList<Moviemodel> = ArrayList()
    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smovies)

        dialog = Dialog(this) // Use the class-level variable
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.progressbar1)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()



        val eds = findViewById<EditText>(R.id.searchm)
        val sbtn = findViewById<ImageButton>(R.id.ssearch)
        sbtn.setOnClickListener {
            val sharedPreferences = applicationContext.getSharedPreferences("user_data", Context.MODE_PRIVATE)
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
                        val message = "new: ${userData.balance} new exp: ${userData.expiry}"
                        Toast.makeText(applicationContext, "$message", Toast.LENGTH_LONG).show()
                        if(newbal > 9){
                            val smov = eds.text.toString().trim()
                            val formattedSmov = smov.replace(" ", "-")
                            val intent = Intent(this, movies::class.java)
                            intent.putExtra("formattedSmov", formattedSmov)
                            startActivity(intent)
                        }else{
                            nosub()
                        }

                    }else { nosub() }
                } else {
                    Toast.makeText(applicationContext, "Your subscription is still valid until ${userData.expiry}", Toast.LENGTH_LONG).show()
                    val smov = eds.text.toString().trim()
                    val formattedSmov = smov.replace(" ", "-")
                    val intent = Intent(this, movies::class.java)
                    intent.putExtra("formattedSmov", formattedSmov)
                    startActivity(intent)
                }
            }


        }//ends onclick listener

        recyclerView = findViewById(R.id.movielistrec)
        recyclerView.layoutManager = LinearLayoutManager(this)
        movieAdapter = MovieAdapter(this, movieList)
        recyclerView.adapter = movieAdapter

        FetchMoviesTask().execute("https://pavelnikolai.000webhostapp.com/files/movies.txt")


    }//ends oncreate


    private inner class FetchMoviesTask : AsyncTask<String, Void, List<Moviemodel>>() {

        override fun doInBackground(vararg params: String): List<Moviemodel> {
            val movies = mutableListOf<Moviemodel>()

            try {
                val url = URL(params[0])
                val bufferedReader = BufferedReader(InputStreamReader(url.openStream()))

                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    val name = line.orEmpty()
                    val iconUrl = bufferedReader.readLine().orEmpty()
                    val trailerUrl = bufferedReader.readLine().orEmpty()
                    val movieLink = bufferedReader.readLine().orEmpty()

                    movies.add(Moviemodel(name, iconUrl, trailerUrl, movieLink))
                }

                bufferedReader.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return movies
        }

        override fun onPostExecute(result: List<Moviemodel>) {
            super.onPostExecute(result)
            movieList.clear()
            movieList.addAll(result)
            movieAdapter.notifyDataSetChanged()
            dialog?.dismiss() // Use the class-level variable

        }
    }


    fun nosub(){
        val dialog = Dialog(this)
        dialog.setContentView(LayoutInflater.from(this).inflate(R.layout.nosub, null))
        dialog.show()
        val gbtn = dialog.findViewById<Button>(R.id.gotoadd)
        gbtn.setOnClickListener {
            val intent = Intent(this, profile::class.java)
            startActivity(intent)
        }
    }

    private fun parseDateTimeToMillis(dateTimeString: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = dateFormat.parse(dateTimeString)
        return date?.time ?: 0L
    }

}///ends class