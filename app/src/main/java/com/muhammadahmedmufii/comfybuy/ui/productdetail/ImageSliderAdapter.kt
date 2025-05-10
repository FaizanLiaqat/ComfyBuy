package com.muhammadahmedmufii.comfybuy.ui.productdetail // Example package name

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter // Use ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.muhammadahmedmufii.comfybuy.R // Import R for accessing drawables and layouts

// Adapter for displaying a list of Bitmap images in a ViewPager2
class ImageSliderAdapter : ListAdapter<Bitmap, ImageSliderAdapter.ImageViewHolder>(BitmapDiffCallback()) {

    // ViewHolder to hold the ImageView for a single image slide
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Reference the ImageView from the new item_image_slide.xml layout
        val imageView: ImageView = itemView.findViewById(R.id.imageViewSlide) // Reference the ImageView from item_image_slide.xml
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        // Inflate the dedicated layout for a single image slide (item_image_slide.xml)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slide, parent, false) // *** CORRECTED LAYOUT INFLATION ***
        return ImageViewHolder(view)
    }

    // Replace the contents of a view
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val bitmap = getItem(position) // Get the Bitmap for the current position

        if (bitmap != null) {
            // Set the Bitmap to the ImageView
            holder.imageView.setImageBitmap(bitmap)
        } else {
            // Set a placeholder or error image if the bitmap is null
            holder.imageView.setImageResource(R.drawable.avatar_placeholder) // Replace with your actual placeholder drawable resource
        }

        // TODO: You might want to add click listeners to the image view (e.g., for full-screen view)
    }

    // DiffUtil.ItemCallback for comparing Bitmaps
    class BitmapDiffCallback : DiffUtil.ItemCallback<Bitmap>() {
        override fun areItemsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
            // Compare by object identity (assuming the same Bitmap object means the same image)
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
            // Comparing Bitmap content can be slow.
            // If you have a unique ID or hash for each image, compare that instead.
            // Otherwise, you might need to compare pixels (very inefficient).
            // For simplicity, we'll assume if items are the same, contents are the same,
            // or if the list is updated, submitList handles it.
            return oldItem.sameAs(newItem) // Compares pixel data (can be slow for large images)
        }
    }
}
