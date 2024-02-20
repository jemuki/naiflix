package com.stream.streamx

import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class watchtv : AppCompatActivity() {
    lateinit var webView: WebView
    lateinit var channellink: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watchtv)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        channellink = intent.getStringExtra("channelLink").toString()


        webView = findViewById(R.id.sweb)

        webView.visibility = View.GONE
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Set a WebViewClient to handle navigation within the WebView
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                            // Inject JavaScript to make the video element cover the whole screen
                            val javascriptCode = """
                // Get the elements by their classes and ids
                var favorItemsDiv = document.querySelector('.favorItems');
                var headListDiv = document.getElementById('headList');
                
                if (favorItemsDiv) {
                    favorItemsDiv.style.display = 'none';
                } else {
                    console.error('Element with class "favorItems" not found.');
                }
                
                if (headListDiv) {
                    headListDiv.style.display = 'none';
                } else {
                    console.error('Element with id "headList" not found.');
                }
                
                // Get the elements by their ids
                var headerDiv = document.getElementById('header');
                var itvNavDiv = document.getElementById('itv-nav');
                var navSecondaryDiv = document.getElementById('navSecondary');
                
                // Check if the elements exist and make them invisible
                if (headerDiv) {
                    headerDiv.style.display = 'none';
                } else {
                    console.error('Element with id "header" not found.');
                }
                
                if (itvNavDiv) {
                    itvNavDiv.style.display = 'none';
                } else {
                    console.error('Element with id "itv-nav" not found.');
                }
                
                if (navSecondaryDiv) {
                    navSecondaryDiv.style.display = 'none';
                } else {
                    console.error('Element with id "navSecondary" not found.');
                }
                
                var tvs = document.querySelector('.rateAll');
                if (tvs) {
                    tvs.style.display = 'none';
                } else {
                    console.error('Element with id "tvs" not found.');
                }
                var targetTd = document.querySelector('td[align="left"][valign="top"] > table[width="465"][border="0"][background="http://www.freeintertv.com/modules/Main/image/bg_prog_ind.gif"][height="1076"][cellpadding="0"][cellspacing="0"]');
                if (targetTd) {
                    targetTd.style.display = 'none';
                } else {
                    console.error('Target <td> element not found.');
                }
                var flist =  document.querySelector('.rightblock');
                if (flist) {
                    flist.style.display = 'none';
                } else {
                    console.error('Target <td> element not found.');
                }
                // Find the <td> element with specific attributes
                var bso = document.querySelector('td[valign="top"][background="http://www.freeintertv.com/modules/Main/image/bg_prog_ind.gif"][height="450"]');
                if (bso) {
                    // Your logic here, for example, hiding the element
                    bso.style.display = 'none';
                } else {
                    console.error('Target <td> element not found.');
                }
                var fot =document.getElementById('footer');
                if (fot) {
                    fot.style.display = 'none';
                } else {
                    console.error('Target <td> element not found.');
                }
                  var desctv = document.querySelector('.tvdesc');
                 if (desctv) {
                     desctv.style.display = 'none';
                 } else {
                     console.error('Target <td> element not found.');
                 }
                 // Find the element by its ID
                 var elementxz = document.querySelector('iframe');
                
                // Check if the element was found
                if (elementxz) {
                  // Set the styles for overlaying
                  elementxz.style.position = 'fixed';
                  elementxz.style.top = '0';
                  elementxz.style.left = '0';
                  elementxz.style.width = '100%';
                  elementxz.style.height = '100%';
                  elementxz.style.zIndex = '9999';
                  elementxz.style.backgroundColor = 'rgba(255, 0, 0, 0.5)'; // Optional: Add a semi-transparent background color
                } else {
                  console.error('Element not found.');
                }
                
                
            """.trimIndent()

                            // Execute the JavaScript code
                            view?.evaluateJavascript(javascriptCode, null)
                                    webView.visibility = View.VISIBLE

            }
                    }

        webView.settings.userAgentString =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
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

        if (channellink != null){
            webView.loadUrl(channellink)
        }else{
            startActivity(Intent(applicationContext, channellist::class.java))
        }
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
    }
}
