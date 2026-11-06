package com.histofilipe.reanimecast

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var root: LinearLayout
    private var fullscreenView: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val uri = request.url
                    val scheme = uri.scheme?.lowercase()
                    // WebView cannot navigate intent:// or other external app schemes.
                    // Never execute an intent fallback URL supplied by a third-party page.
                    if (scheme != "http" && scheme != "https") return true
                    // Keep ordinary web navigation in WebView.
                    return false
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                    if (fullscreenView != null) { callback.onCustomViewHidden(); return }
                    fullscreenView = view
                    fullscreenCallback = callback
                    root.removeAllViews()
                    root.addView(view, LinearLayout.LayoutParams(-1, -1))
                    window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                }
                override fun onHideCustomView() {
                    if (fullscreenView == null) return
                    root.removeAllViews()
                    fullscreenView = null
                    fullscreenCallback?.onCustomViewHidden()
                    fullscreenCallback = null
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    root.addView(nav, LinearLayout.LayoutParams(-1, -2))
                    root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
                }
            }
        }
        fun addButton(label: String, action: () -> Unit) {
            nav.addView(Button(this).apply { text = label; setOnClickListener { action() } },
                LinearLayout.LayoutParams(0, -2, 1f))
        }
        addButton("Voltar") { if (webView.canGoBack()) webView.goBack() }
        addButton("Avançar") { if (webView.canGoForward()) webView.goForward() }
        addButton("Atualizar") { webView.reload() }
        root.addView(nav, LinearLayout.LayoutParams(-1, -2))
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        webView.loadUrl("https://reanime.to/")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            fullscreenView != null -> webView.webChromeClient?.onHideCustomView()
            webView.canGoBack() -> webView.goBack()
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
