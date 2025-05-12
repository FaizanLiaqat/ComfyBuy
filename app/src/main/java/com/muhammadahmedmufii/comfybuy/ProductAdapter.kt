package com.muhammadahmedmufii.comfybuy // Use your main package name

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter // Use ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.muhammadahmedmufii.comfybuy.domain.model.Product // Import your domain Product model
import com.muhammadahmedmufii.comfybuy.R // Import R for accessing drawables (like placeholders)


// Use ListAdapter for efficient list updates when the list of products changes.
// It takes the Product domain model and a ViewHolder.
class ProductAdapter(
    // Lambda function to handle item clicks. It receives the clicked Product object.
    private val onItemClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) { // Inherit from ListAdapter
    private val TAG = "ProductAdapter" // Logging Tag
    // ViewHolder class to hold the views for a single product item (item_product.xml)
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // References to the UI elements in item_product.xml
        val productImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val productName: TextView = itemView.findViewById(R.id.tvProductName)
        val productPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val productLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val productDistance: TextView = itemView.findViewById(R.id.tvDistance) // Assuming you have this ID
        // Add other views from item_product.xml as needed (e.g., favorite icon)
    }

    // Create new views (invoked by the layout manager when it needs a new ViewHolder)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        // Inflate the layout for a single product item (item_product.xml)
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager to display data at a specific position)
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position) // Get the Product object for the current position from the ListAdapter

        // Bind data from the Product domain model to the ViewHolder's views
        holder.productName.text = product.title // Use 'title' from domain model
        holder.productPrice.text = product.price // Use 'price' from domain model (as String)
        holder.productLocation.text = product.location // Use 'location' from domain model

        // TODO: Calculate and display distance if location string needs processing
        // For now, set a placeholder or process the location string to display distance.
        holder.productDistance.text = "Distance N/A" // Placeholder

        // --- CORRECTED IMAGE HANDLING ---
        // Check if the list of image bitmaps is not empty
        Log.i(TAG, "onBindViewHolder: Binding product '${product.title}' (ID: ${product.productId}), total imageBitmaps: ${product.imageBitmaps.size}")
        if (product.imageBitmaps.isNotEmpty()) {
            val firstBitmap = product.imageBitmaps.firstOrNull()
            if (firstBitmap != null) {
                Log.d(TAG, "onBindViewHolder: Setting firstBitmap (width: ${firstBitmap.width}, height: ${firstBitmap.height}) for '${product.title}'")
                holder.productImage.setImageBitmap(firstBitmap)
            } else {
                Log.w(TAG, "onBindViewHolder: First bitmap is null for '${product.title}' even though imageBitmaps list is not empty. Setting placeholder.")
                holder.productImage.setImageResource(R.drawable.avatar_placeholder)
            }
        } else {
            Log.d(TAG, "onBindViewHolder: No images for '${product.title}', setting placeholder.")
            holder.productImage.setImageResource(R.drawable.avatar_placeholder)
        }
        // --- END OF CORRECTED IMAGE HANDLING ---

        // Set click listener for the entire item view
        holder.itemView.setOnClickListener {
            onItemClick(product) // Invoke the lambda function passed in the constructor with the clicked Product
        }

        // TODO: Handle favorite icon state and click listener if applicable
        // If you add a favorite field to your Product domain model and a favorite icon in item_product.xml,
        // you would update the icon's appearance here and set a click listener on it.
    }

    // DiffUtil.ItemCallback is used by ListAdapter to efficiently calculate the differences
    // between two lists when submitList() is called. This helps in updating only the
    // changed items in the RecyclerView, improving performance.
    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            // Items are considered the same if their unique identifiers are the same.
            // We use the productId from the domain model.
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            // Contents are considered the same if all relevant properties are equal.
            // Since our Product data class is a data class, its equals() method
            // automatically compares all properties, which is suitable here.
            return oldItem == newItem
        }
    }

    // Note: When using ListAdapter, you update the list by calling submitList(newList)
    // on the adapter instance from your Activity or Fragment. You do NOT override getItemCount()
    // or use notifyDataSetChanged() directly. ListAdapter handles this automatically.
}
