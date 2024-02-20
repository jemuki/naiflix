package com.stream.streamx

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class spotify : AppCompatActivity() {

    lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spotify)

        webView = findViewById(R.id.sweb)

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Set a WebViewClient to handle navigation within the WebView
        webView.webViewClient = WebViewClient()
        webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.enableSmoothTransition()
        webView.settings.setSupportMultipleWindows(true)
        webView.settings.loadsImagesAutomatically = true
        webView.visibility = View.GONE
        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            WebView.enableSlowWholeDocumentDraw()
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.loadUrl("https://spotify.com")
      //  webView.visibility = View.GONE
//        val imageView = ImageView(this)
//        imageView.layoutParams = ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT
//        )
//
//        // Set drawable image to ImageView
//        imageView.setImageResource(R.drawable.spss )
//
//        // Set scale type as needed (e.g., FIT_CENTER)
//        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
//        setContentView(imageView)

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

        val sharedPreferences = applicationContext.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userDataJson = sharedPreferences?.getString("userData", null)

        val gson = Gson()
        val userData = gson.fromJson(userDataJson, UserData::class.java)
        if(userData.expiry == ""){
            nosub()
        }else{
            //check for validity
            if (System.currentTimeMillis() > parseDateTimeToMillis(userData.expiry)) {
                if (userData.balance >9){
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
                        webView.visibility = View.VISIBLE
                    }else{
                        nosub()
                    }

                }else{ nosub() }

            } else {
                webView.visibility = View.VISIBLE
            }
        }


    }//ends oncreate


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

}//ends class
