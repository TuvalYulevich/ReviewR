package com.example.reviewr.Utils

import android.app.Application
import com.cloudinary.android.MediaManager

class CloudinaryUtils : Application() {
    override fun onCreate() {
        super.onCreate()
        val config: HashMap<String, String> = hashMapOf(
            "cloud_name" to "dm8sulfig",  // Replace with your Cloudinary cloud name
            "api_key" to "129181168733979",  // Replace with your Cloudinary API key
            "api_secret" to "uNaILxRogPyZ_FTQtnOWEQ-Tq5Y"  // Replace with your Cloudinary API secret
        )
        MediaManager.init(this, config)
    }
}
