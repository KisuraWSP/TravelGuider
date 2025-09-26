package com.app.tourism_app.database.data.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.tourism_app.R
import com.app.tourism_app.database.model.Favorite

class FavoriteAdapter(
    private val onClick: (Favorite) -> Unit
) : ListAdapter<Favorite, FavoriteAdapter.VH>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tv_fav_title)
        private val subtitle: TextView = itemView.findViewById(R.id.tv_fav_subtitle)

        fun bind(f: Favorite) {
            title.text = f.title
            subtitle.text = f.description ?: ""
            itemView.setOnClickListener { onClick(f) }
        }
    }

    class Diff : DiffUtil.ItemCallback<Favorite>() {
        override fun areItemsTheSame(oldItem: Favorite, newItem: Favorite) = oldItem.placeId == newItem.placeId
        override fun areContentsTheSame(oldItem: Favorite, newItem: Favorite) = oldItem == newItem
    }
}