package com.google.mintyfreshcreations12.hydrangea

import android.graphics.Bitmap
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class GalleryAdapter(val bitmaps: MutableList<Pair<String, Bitmap>>, val imageCallback: (id: String) -> Unit) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        return GalleryViewHolder(from(parent.context).inflate(R.layout.gallery_image, parent, false))
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.itemView.findViewById<CardView>(R.id.imageCard).setOnClickListener{
            imageCallback.invoke(bitmaps[position].first)
        }
        holder.itemView.findViewById<ImageView>(R.id.image).setImageBitmap(bitmaps[position].second)
    }

    override fun getItemCount(): Int {
        return bitmaps.size
    }

    class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}