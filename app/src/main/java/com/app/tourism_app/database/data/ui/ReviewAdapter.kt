package com.app.tourism_app.database.data.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.tourism_app.R
import com.app.tourism_app.database.model.Review

class ReviewAdapter : ListAdapter<Review, ReviewAdapter.VH>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUser = itemView.findViewById<TextView>(R.id.tv_user)
        private val rbRating = itemView.findViewById<RatingBar>(R.id.rb_rating)
        private val tvComment = itemView.findViewById<TextView>(R.id.tv_comment)

        fun bind(review: Review) {
            tvUser.text = review.userId
            rbRating.rating = review.rating.toFloat()
            tvComment.text = review.comment
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Review, newItem: Review) = oldItem == newItem
    }
}
