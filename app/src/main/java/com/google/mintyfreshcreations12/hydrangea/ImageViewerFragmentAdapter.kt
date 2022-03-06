package com.google.mintyfreshcreations12.hydrangea

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.ArrayList


class ImageViewerFragmentAdapter(fragmentActivity: FragmentActivity, private val images: ArrayList<String>?, private val hydrus: HydrusApi) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return (images?.size ?: 0)
    }

    override fun createFragment(position: Int): Fragment {
        return ImageViewerFragment(images?.get(position) ?: "", hydrus)
    }

    class ImageViewerFragment(private val id: String, private val hydrus: HydrusApi) : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.viewer_image, container, false)
            hydrus.loadImage(id) {
                val imageView = view.findViewById<ImageView>(R.id.imageView)
                view.findViewById<ImageView>(R.id.imageView).setImageBitmap(it)
            }
            return view
        }
    }
}