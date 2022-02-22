package com.google.mintyfreshcreations12.hydrangea

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2

class ViewImage : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)
        val prefs = getSharedPreferences(Constants.PREFS, MODE_PRIVATE)

        val hydrus = HydrusApi(this, prefs.getString(Constants.PREF_HYDRUS_BASE_URL, "").toString(),
            prefs.getString(Constants.PREF_API_KEY, "").toString())
        val ids = this.intent.extras?.getStringArrayList(Constants.BNDL_IMAGE_LIST)?: ArrayList()
        findViewById<ViewPager2>(R.id.viewPager).adapter = ImageViewerFragmentAdapter(
            this, ids, hydrus)
        findViewById<ViewPager2>(R.id.viewPager).setCurrentItem(ids.indexOf(this.intent.extras?.getString(Constants.BNDL_IMAGE_ID)), false)

    }
}