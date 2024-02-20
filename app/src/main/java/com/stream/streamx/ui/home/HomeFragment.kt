package com.stream.streamx.ui.home

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.stream.streamx.databinding.FragmentHomeBinding
import android.os.AsyncTask
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stream.streamx.MatchAdapter
import com.stream.streamx.MatchDetails
import com.stream.streamx.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

class HomeFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var adapter: MatchAdapter? = null
    private var dialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        dialog = Dialog(requireContext()) // Use the class-level variable
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.progressbar1)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()

        recyclerView = view.findViewById(R.id.matchDetailsRecyclerView)
        adapter = MatchAdapter(requireContext(),emptyList())

        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = adapter

        // Replace the URL with the actual URL of your text file
        val fileUrl = "https://pavelnikolai.000webhostapp.com/files/matches.txt"

        // Use AsyncTask to perform the network operation
        ReadOnlineFileTask().execute(fileUrl)

        return view
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
                Toast.makeText(requireContext(), "the size is, ${result.size}", Toast.LENGTH_LONG).show()
                adapter?.setData(result)
                dialog?.dismiss() // Use the class-level variable

            }
        }
    }
}
