package com.app.tourism_app.database.data.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.tourism_app.R
import com.app.tourism_app.database.data.ui.LocationUi

class LocationsAdapter(
    private val onItemClick: (LocationUi) -> Unit
) : ListAdapter<LocationUi, LocationsAdapter.VH>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tv_title)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.rb_rating)
        private val subtitle: TextView = itemView.findViewById(R.id.tv_subtitle)

        fun bind(loc: LocationUi) {
            Log.d("Adapter", "Binding: ${loc.title}")
            title.text = loc.title
            subtitle.text = loc.description ?: ""
            ratingBar.rating = loc.averageRating
            itemView.setOnClickListener { onItemClick(loc) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<LocationUi>() {
        override fun areItemsTheSame(oldItem: LocationUi, newItem: LocationUi): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: LocationUi, newItem: LocationUi): Boolean =
            oldItem == newItem
    }
}
