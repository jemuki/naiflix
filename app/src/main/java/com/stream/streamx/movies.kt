package com.stream.streamx

import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

class movies : AppCompatActivity() {

    private lateinit var webView: WebView
    private var currentPlayLink: String? = null
    var chosenl: String? = null
    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movies)

        dialog = Dialog(this) // Use the class-level variable
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.progressbar1)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()

//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        webView = findViewById(R.id.mweb)
        webView.addJavascriptInterface(WebAppInterface(this), "Android")
        webView.visibility = View.GONE
        chosenl = intent.getStringExtra("chosenlink")
        if (chosenl != null){
            watchnow(chosenl!!)
        }

        val formattedSmov = intent.getStringExtra("formattedSmov")
        LoadWebPageTask().execute("https://musichq.pe/search/$formattedSmov") // Replace with the actual URL
        //LoadWebPageTask().execute("https://musichq.pe/search/fast-x") // Replace with the actual URL

        val refresh = findViewById<ImageButton>(R.id.imageButtonref)
        refresh.setOnClickListener {
            refreshWebViewUrl()
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Check if the orientation is landscape
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // You might want to handle landscape mode differently if needed
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // You might want to handle portrait mode differently if needed
        }
    }


    private inner class LoadWebPageTask : AsyncTask<String, Void, List<Triple<String, String, String>>>() {

        override fun doInBackground(vararg params: String): List<Triple<String, String, String>> {
            val itemsWithDetails = mutableListOf<Triple<String, String, String>>()
            try {
                val document: Document = Jsoup.connect(params[0]).get()
                val elements = document.select("div.flw-item")

                for (element in elements) {
                    val linkElement = element.selectFirst("a[href]")
                    val link = linkElement?.attr("href")?.let { "https://musichq.pe/$it" } ?: ""
                    val name = linkElement?.attr("title") ?: ""

                    // Fetch the data-src attribute for lazy-loaded images
                    val imgElement = element.selectFirst("img.film-poster-img")
                    val imageUrl = imgElement?.attr("data-src") ?: ""
                    Log.d("poster", "my poster is $imageUrl")

                    itemsWithDetails.add(Triple(name, link, imageUrl))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return itemsWithDetails
        }

        override fun onPostExecute(result: List<Triple<String, String, String>>) {
            // Display AlertDialog with the links, names, and images
            showAlertDialog(result)
        }

    }

    private fun showAlertDialog(itemsWithDetails: List<Triple<String, String, String>>) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Select movie to watch")

                                                                                                                                                                                                                    if (itemsWithDetails.isNotEmpty()) {
            val adapter = MovieAdapter(this, itemsWithDetails)
            alertDialog.setAdapter(adapter) { _, which ->
                val selectedItem = itemsWithDetails[which]
                val selectedLink = selectedItem.second

                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                loadLinkInWebView(selectedLink)
            }

            val dialog: AlertDialog = alertDialog.create()
            dialog.show()
        } else {
            alertDialog.setMessage("Movie not found, try refining your search.")
            alertDialog.setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
            }

            val dialog: AlertDialog = alertDialog.create()
            dialog.show()
        }
    }

    private class MovieAdapter(
        private val context: Context,
        private val itemsWithDetails: List<Triple<String, String, String>>
    ) : ArrayAdapter<String>(
        context, R.layout.movie_list_item, itemsWithDetails.map { it.first }.toTypedArray()
    ) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.movie_list_item, parent, false)

            val imageView: ImageView = view.findViewById(R.id.movieIcon)
            val textView: TextView = view.findViewById(R.id.movieName)

            // Load the image using Picasso or Glide
            val imageUrl = itemsWithDetails[position].third
            if (imageUrl.isNotEmpty()) {
                Picasso.get().load(imageUrl).placeholder(R.drawable.baseline_search_24).into(imageView)
            } else {
                // Set a placeholder image or handle the case where the URL is empty
                imageView.setImageResource(R.drawable.baseline_search_24)
            }
            // Set the movie name
            textView.text = itemsWithDetails[position].first

            return view
        }
    }

    private fun loadLinkInWebView(link: String) {
        AsyncTask.execute {
            try {
                val document: Document = Jsoup.connect(link).get()
                val playLinkElement = document.selectFirst("a.dp-w-c-play")
                val playLink = playLinkElement?.attr("href")?.let { "https://musichq.pe/$it" }
                currentPlayLink = playLink
                runOnUiThread {
                    if (!playLink.isNullOrBlank() && !playLink.contains("javascript")) {
                        webView.webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString()

                                // Block redirects and only allow URLs related to the video
                                return if (url != null && (url.contains("musichq.pe") || url.startsWith("javascript:"))) {
                                    false // Allow loading within musichq.pe or JavaScript URLs
                                } else {
                                    // Prevent loading external URLs
                                    true
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)

                                // Inject JavaScript to manipulate the DOM
                                val manipulateDomJs = """
            var contentDiv = document.querySelector('.watching_player-area');
            if (contentDiv) {
                // Remove other elements from the body
                var bodyChildren = document.body.children;
                for (var i = 0; i < bodyChildren.length; i++) {
                    var child = bodyChildren[i];
                    if (child !== contentDiv) {
                        document.body.removeChild(child);
                    }
                }
                document.body.appendChild(contentDiv);
            }
            
            // Get the parent element of the body tag
            var parentElement = document.body.parentElement;

            // Get all iframes within the parent element
            var iframesOutsideBody = parentElement.querySelectorAll('iframe');

            // Hide the second iframe if it exists
            if (iframesOutsideBody.length > 1) {
                var secondIframe = iframesOutsideBody[4];
                secondIframe.style.display = 'none';
            }

           
            
        """.trimIndent()

                                // Execute the JavaScript code
                                webView.evaluateJavascript(manipulateDomJs, null)
                                webView.visibility = View.VISIBLE
                                dialog?.dismiss() // Use the class-level variable

                            }
                        }
                        webView.webChromeClient = object : WebChromeClient() {
                            // Override onJsAlert to suppress alerts
                            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                                // Suppress alerts
                                result?.confirm()
                                return true
                            }
                        }


                        webView.webChromeClient = WebChromeClient()

                        // Set the user agent to simulate a desktop browser
                        val desktopUserAgent =
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
                        webView.settings.userAgentString = desktopUserAgent
                        webView.settings.javaScriptEnabled = true
                        webView.settings.domStorageEnabled = true
                        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
                        webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
                        webView.settings.mediaPlaybackRequiresUserGesture = false
                        webView.settings.enableSmoothTransition()
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)

                        if (Build.VERSION.SDK_INT >= 19) {
                            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        } else {
                            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                        }

                        // Load the URL
                        webView.loadUrl(playLink)
                        tell()
                    } else {
                        // Show dialog indicating that the movie is not yet available
                        showNotAvailableDialog()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // Handle the exception
            }
        }
    }

    private fun showNotAvailableDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Movie Not Available")
        alertDialog.setMessage("Sorry, the movie is not yet available.")
        alertDialog.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = alertDialog.create()
        dialog.show()
    }

    private fun refreshWebViewUrl() {
    //webView.reload()
        recreate()
    }

    private fun tell(){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Note")
        alertDialog.setMessage("In case of connectivity issues refresh with the button on top right.")
        alertDialog.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = alertDialog.create()
        dialog.show()

    }


    public fun watchnow(thelink: String){
        AsyncTask.execute {
            try {
                runOnUiThread {
                    if (thelink != "") {
                        webView.webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString()

                                // Block redirects and only allow URLs related to the video
                                return if (url != null && (url.contains("musichq.pe") || url.startsWith("javascript:"))) {
                                    false // Allow loading within musichq.pe or JavaScript URLs
                                } else {
                                    // Prevent loading external URLs
                                    true
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)

                                // Inject JavaScript to manipulate the DOM
                                val manipulateDomJs = """
         
                    var contentDiv = document.querySelector('.watching_player-area');
                    if (contentDiv) {
                        // Remove other elements from the body
                        var bodyChildren = document.body.children;
                        for (var i = 0; i < bodyChildren.length; i++) {
                            var child = bodyChildren[i];
                            if (child !== contentDiv) {
                                document.body.removeChild(child);
                            }
                        }
                        document.body.appendChild(contentDiv);
                    }
                    
                      // Get the parent element of the body tag
            var parentElement = document.body.parentElement;

            // Get all iframes within the parent element
            var iframesOutsideBody = parentElement.querySelectorAll('iframe');

            if (iframesOutsideBody.length > 1) {
                var secondIframe = iframesOutsideBody[2];
                secondIframe.style.display = 'none';
            }

         
        """.trimIndent()

                                // Execute the JavaScript code
                                webView.evaluateJavascript(manipulateDomJs, null)
                                webView.visibility = View.VISIBLE
                                dialog?.dismiss() // Use the class-level variable

                            }
                        }

                        webView.webChromeClient = object : WebChromeClient() {
                            // Override onJsAlert to suppress alerts
                            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                                // Suppress alerts
                                result?.confirm()
                                return true
                            }
                        }


                        webView.webChromeClient = WebChromeClient()

                        // Set the user agent to simulate a desktop browser
                        val desktopUserAgent =
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
                        webView.settings.userAgentString = desktopUserAgent
                        webView.settings.javaScriptEnabled = true
                        webView.settings.domStorageEnabled = true
                        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
                        webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
                        webView.settings.mediaPlaybackRequiresUserGesture = false
                        webView.settings.enableSmoothTransition()
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)

                        if (Build.VERSION.SDK_INT >= 19) {
                            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        } else {
                            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                        }

                        // Load the URL

                        webView.loadUrl(thelink)
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        tell()

                    } else {
                        // Show dialog indicating that the movie is not yet available
                        showNotAvailableDialog()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // Handle the exception
            }
        }

    }

}
//https://musichq.pe/watch-movie/oppenheimer-98446.9759013
class WebAppInterface(private val context: movies) {

    @JavascriptInterface
    fun showElementInfo(elementInfo: String) {
        Log.d("WebAppInterface", "showElementInfo called: $elementInfo")

        // Display a Toast with information about the clicked element
        context.runOnUiThread {
            Toast.makeText(context, "Clicked Element Info: $elementInfo", Toast.LENGTH_SHORT).show()
            Log.d("the class", "$elementInfo")
        }
    }
}
