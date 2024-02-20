package com.stream.streamx

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val handler = Handler()
    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        initializeWebView()
        webView.visibility = View.GONE
        dialog = Dialog(this) // Use the class-level variable
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.progressbar1)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()

        val id = intent.getStringExtra("id")
        val link = intent.getStringExtra("link")

        if (id != null && link != null) {
            // Use id and link as needed, e.g., load the URL in a WebView
            webView.loadUrl(link)
        }
    }

    private fun initializeWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)

        // Set a custom user-agent to disguise the WebView as a desktop browser
        val userAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
        webView.settings.userAgentString = userAgent

        // Allow autoplay for videos
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.enableSmoothTransition()
        webView.settings.setSupportMultipleWindows(true)
        webView.settings.loadsImagesAutomatically = true

        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            WebView.enableSlowWholeDocumentDraw()
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = createWebViewClient()
    }

    private fun createWebViewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // Block redirects by returning true
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Apply the CSS trick for hardware acceleration
                view?.loadUrl("javascript:(function() { document.body.style.transform = 'translate3d(0,0,0)'; })();")
                val javascriptCode = """
                    // Get all buttons on the page
                    const buttons = document.getElementsByTagName('button');
                    
                    // Iterate over each button
                    for (let i = 0; i < buttons.length; i++) {
                        const button = buttons[i];
                        // Check if the button text contains "UNMUTE"
                        if (button.innerText.includes('UNMUTE')) {
                            // Click on the button
                            button.click();
                            // Hide the button
                            button.style.display = 'none';
                            // Exit the loop since we found the button
                            break;
                        }
                    }
                    
                    // Override JavaScript functions to block popups
                    window.alert = function() {};
                    window.confirm = function() { return true; };
                    window.prompt = function() { return null; };
                    
                    // Remove iframes
                    const iframes = document.getElementsByTagName('iframe');
                    for (let i = 0; i < iframes.length; i++) {
                        const iframe = iframes[i];
                        iframe.parentNode.removeChild(iframe);
                    }
                """.trimIndent()
                // Auto-play the video if it exists on the page
                view?.evaluateJavascript(javascriptCode, null)
                webView.visibility = View.VISIBLE
                dialog?.dismiss()
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url.toString()
                Log.d("minter", "Request intercepted: $url")
                if (shouldBlockRequest(url)) {
                    Log.d("minter", "Blocking request: $url")
                    // Returning an empty response to block the request
                    return WebResourceResponse("text/plain", "utf-8", null)
                } else {
                    Log.d("minter", "Allowing request: $url")
                    // Allow the request by returning null
                    return null
                }
            }

        }
    }

    private fun shouldBlockRequest(url: String): Boolean {
        // Load ad servers from assets
        val adServers = loadAdServersFromAssets(this)
        // Check if the URL matches any of the ad servers
        adServers.forEach { adServer ->
            if (url.contains(adServer)) {
                Log.d("minter", "the adserver is: $adServer")
                Log.d("minter", "Blocking request: $url matched with ad server: $adServer")
                return true
            }
        }
        return false
    }

    private fun loadAdServersFromAssets(context: Context): List<String> {
        val adServers = mutableListOf<String>()
        try {
            val inputStream = context.assets.open("adservers.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = reader.readLine()
            while (line != null) {
                val adServer = line.trim()
                if (adServer.isNotEmpty()) {
                    adServers.add(adServer)
                }
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return adServers
    }


    // Utility function to show a toast
    private fun showToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
