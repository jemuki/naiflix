package com.stream.streamx

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.*

class games : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null
    private var adapter: MatchAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games)

        recyclerView = findViewById(R.id.matchDetailsRecyclerView)
        adapter = MatchAdapter(this,emptyList())

        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = adapter

        // Replace the URL with the actual URL of your text file
        val fileUrl = "https://pavelnikolai.000webhostapp.com/files/matches.txt"

        // Use AsyncTask to perform the network operation
        ReadOnlineFileTask().execute(fileUrl)
    }

    private inner class ReadOnlineFileTask :
        AsyncTask<String?, Void?, List<MatchDetails?>?>() {
        override fun doInBackground(vararg params: String?): List<MatchDetails?>? {
            val fileUrl = params[0]
            return try {
                val url = URL(fileUrl)
                val reader = BufferedReader(InputStreamReader(url.openStream()))
                val itemList: MutableList<MatchDetails?> = mutableListOf()
                var line: String?
                var currentMatch: MatchDetails? = null
                var count = 0

                while (reader.readLine().also { line = it } != null) {
                    if (line == "XXXX") {
                        // If "XXXX" is encountered, it means the start of a new match
                        currentMatch?.let {
                            itemList.add(it)
                            Log.d("MatchDetails", "Added match: $it")
                        }
                        currentMatch = MatchDetails()
                        count = 0
                    } else {
                        // Assuming each line contains a specific piece of information
                        when (count) {
                            0 -> currentMatch?.id = line?.takeIf { it.isNotBlank() }.toString()
                            1 -> currentMatch?.team1 = line?.takeIf { it.isNotBlank() }.toString()
                            2 -> currentMatch?.team2 = line?.takeIf { it.isNotBlank() }.toString()
                            3 -> currentMatch?.time = line?.takeIf { it.isNotBlank() }.toString()
                            4 -> currentMatch?.imglUrl = line?.takeIf { it.isNotBlank() }.toString()
                        }
                        count++
                    }
                }

                // Add the last match after the loop ends
                currentMatch?.let {
                    itemList.add(it)
                    Log.d("MatchDetails", "Added last match: $it")
                }

                reader.close()
                itemList
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(result: List<MatchDetails?>?) {
            super.onPostExecute(result)
            if (result != null) {
                Toast.makeText(applicationContext, "the size is, ${result.size}", Toast.LENGTH_LONG).show()
                adapter?.setData(result)
                recyclerView?.layoutManager = LinearLayoutManager(this@games)
                recyclerView?.adapter = adapter
//                adapter?.setContext(this@games)
            }
        }
    }

}
