package com.example.reviewr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.reviewr.R

class ReviewAdapter(
    private val reviews: List<Map<String, Any>>,
    private val onEditClicked: (Map<String, Any>) -> Unit,
    private val onDeleteClicked: (String) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.reviewTitle)
        val description: TextView = itemView.findViewById(R.id.reviewDescription)
        val status: TextView = itemView.findViewById(R.id.reviewStatus)
        val category: TextView = itemView.findViewById(R.id.reviewCategory)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.title.text = review["title"] as? String ?: "No Title"
        holder.description.text = review["description"] as? String ?: "No Description"
        holder.status.text = "Status: ${review["status"] as? String ?: "Unknown"}"
        holder.category.text = "Category: ${review["category"] as? String ?: "Unknown"}"

        holder.editButton.setOnClickListener { onEditClicked(review) }
        holder.deleteButton.setOnClickListener {
            val postId = review["postId"] as? String ?: return@setOnClickListener
            onDeleteClicked(postId)
        }
    }


    override fun getItemCount(): Int = reviews.size
}
