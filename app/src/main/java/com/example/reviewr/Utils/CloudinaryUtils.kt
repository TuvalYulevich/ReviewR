package com.example.reviewr.Utils

import android.app.Application
import com.cloudinary.android.MediaManager

// Cloudinary API
class CloudinaryUtils : Application() {
    override fun onCreate() {
        super.onCreate()
        val config: HashMap<String, String> = hashMapOf(
            "cloud_name" to "dm8sulfig",
            "api_key" to "129181168733979",
            "api_secret" to "uNaILxRogPyZ_FTQtnOWEQ-Tq5Y"
        )
        MediaManager.init(this, config)
    }
}
