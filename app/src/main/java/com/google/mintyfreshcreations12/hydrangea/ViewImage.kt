package com.google.mintyfreshcreations12.hydrangea

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class ViewImage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)
        val prefs = getSharedPreferences(Constants.PREFS, MODE_PRIVATE)

        val hydrus = HydrusApi(this, prefs.getString(Constants.PREF_HYDRUS_BASE_URL, "").toString(),
            prefs.getString(Constants.PREF_API_KEY, "").toString())

        hydrus.loadImage(this.intent.extras?.getString(Constants.BNDL_IMAGE_ID).toString()) {
            findViewById<ImageView>(R.id.imageView).setImageBitmap(it)
        }
    }
}