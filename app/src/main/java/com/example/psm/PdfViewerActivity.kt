package com.example.psm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PdfViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        val path = intent.getStringExtra("path")

        val webView = findViewById<WebView>(R.id.webView)

        val pdfPath = "file:/${path}"

        val READ_EXTERNAL_STORAGE_REQUEST_CODE = 123

        // Check if the READ_EXTERNAL_STORAGE permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_REQUEST_CODE)
        } else {
            webView.settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                cacheMode = WebSettings.LOAD_DEFAULT
                domStorageEnabled = true
            }
            webView.webViewClient = WebViewClient()
            webView.loadUrl(pdfPath)

            println(pdfPath)
            println("https://docs.google.com/gview?embedded=true&url=$pdfPath")

            // Ensure links open in the WebView instead of the default browser
            webView.webViewClient = WebViewClient()
        }
    }
}
