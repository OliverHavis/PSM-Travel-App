package com.example.psm.models

import android.app.DownloadManager.Query

class Saved(
    val id: String,
    val destination: Destination,
    val user_id: String,
    val query: Map<String, Any>) {
}
