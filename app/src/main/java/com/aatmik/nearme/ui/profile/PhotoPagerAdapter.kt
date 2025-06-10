package com.aatmik.nearme.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.aatmik.nearme.R
import com.aatmik.nearme.model.UserPhoto
import com.bumptech.glide.Glide

class PhotoPagerAdapter(private val photos: List<UserPhoto>) :
        RecyclerView.Adapter<PhotoPagerAdapter.PhotoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view =
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_profile_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]
        Glide.with(holder.imageView.context)
                .load(photo.url)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .centerCrop()
                .into(holder.imageView)
    }

    override fun getItemCount(): Int = photos.size

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivProfilePhoto)
    }
}
