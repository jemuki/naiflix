package com.stream.streamx.ui.dashboard

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.stream.streamx.R
import com.stream.streamx.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private var dialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        dialog = Dialog(requireContext()) // Use the class-level variable
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.progressbar1)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()

        val webView: WebView = binding.webr
        webView.visibility = View.GONE
        // Set up WebView settings
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // Execute your JavaScript code here
                val javascriptCode = """
                      var head1 = document.querySelector('.site-header');
                     if (head1) {
                         head1.style.display = 'none';
                     } else {
                         console.error('Target  element not found.');
                     }
                      var head2 = document.querySelector('.advert--banner-wrap-ghost');
                     if (head2) {
                         head2.style.display = 'none';
                     } else {
                         console.error('Target  element not found.');
                     }
                     var head3 = document.querySelector('.page-header');
                     if (head3) {
                         head3.style.display = 'none';
                     } else {
                         console.error('Target  element not found.');
                     }
                     var head4 = document.querySelector('.page-nav');
                     if (head4) {
                         head4.style.display = 'none';
                     } else {
                         console.error('Target  element not found.');
                     }
                     var head5 = document.querySelector('.section-nav__head');
                     if (head5) {
                         head5.style.display = 'none';
                     } else {
                         console.error('Target  element not found.');
                     }
                    var notice = document.getElementById('notice');
                    if (notice) {
                        notice.style.display = 'none';
                    } else {
                        console.error('Target element not found.');
                    }
                
                  """.trimIndent()

                webView.evaluateJavascript(javascriptCode, null)
                webView.visibility = View.VISIBLE
                dialog?.dismiss() // Use the class-level variable

            }
        }
        webView.settings.javaScriptEnabled = true

        // Example: Load a website link in WebView
        val websiteLink = "https://www.skysports.com/football-results"
        webView.loadUrl(websiteLink)

        dashboardViewModel.text.observe(viewLifecycleOwner) {
            // You can update other UI elements based on the ViewModel data if needed
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
