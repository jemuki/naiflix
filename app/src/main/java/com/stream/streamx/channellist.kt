package com.stream.streamx

import android.app.Dialog
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class channellist : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cadapter: Cadapter
    private var channellist: MutableList<ChannelModel> = ArrayList()
    private var dialog: Dialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channellist)

        dialog = Dialog(this) // Use the class-level variable
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.progressbar1)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()


        recyclerView = findViewById(R.id.chanlist)
        recyclerView.layoutManager = LinearLayoutManager(this)
        cadapter = Cadapter(this, channellist)
        recyclerView.adapter = cadapter

        FetchchannelsTask().execute("https://pavelnikolai.000webhostapp.com/files/tv.txt")

    }//ends oncreate

    private inner class FetchchannelsTask : AsyncTask<String, Void, List<ChannelModel>>() {

        override fun doInBackground(vararg params: String): List<ChannelModel> {
            val chans = mutableListOf<ChannelModel>()

            try {
                val url = URL(params[0])
                val bufferedReader = BufferedReader(InputStreamReader(url.openStream()))

                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    val tvname = line.orEmpty() // Use the current line for channel name
                    val tvicon = bufferedReader.readLine().orEmpty()
                    val tvlinks = bufferedReader.readLine().orEmpty()

                    chans.add(ChannelModel(tvname, tvicon, tvlinks))
                }

                bufferedReader.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return chans
        }

        override fun onPostExecute(result: List<ChannelModel>) {
            super.onPostExecute(result)
            channellist.clear()
            channellist.addAll(result)
            cadapter.notifyDataSetChanged()
            dialog?.dismiss() // Use the class-level variable

        }
    }


}//ends class