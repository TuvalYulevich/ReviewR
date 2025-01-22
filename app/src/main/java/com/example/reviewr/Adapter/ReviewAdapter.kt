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
    private val showEditDeleteButtons: Boolean = true,
    private val onEditClicked: ((Map<String, Any>) -> Unit)? = null, // Nullable callback
    private val onDeleteClicked: ((String) -> Unit)? = null, // Nullable callback
    private val onItemClicked: ((String) -> Unit)? = null // Nullable callback
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.reviewTitle)
        val description: TextView = itemView.findViewById(R.id.reviewDescription)
        val status: TextView = itemView.findViewById(R.id.reviewStatus)
        val category: TextView = itemView.findViewById(R.id.reviewCategory)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val postId = reviews[position]["postId"] as? String
                    postId?.let { onItemClicked?.invoke(it) } // Call item click listener
                }
            }
        }
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

        // Set visibility and click listeners for edit and delete buttons
        if (showEditDeleteButtons) {
            holder.editButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE
            holder.editButton.setOnClickListener { onEditClicked?.invoke(review) }
            holder.deleteButton.setOnClickListener {
                val postId = review["postId"] as? String ?: return@setOnClickListener
                onDeleteClicked?.invoke(postId)
            }
        } else {
            holder.editButton.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE
        }
    }
    override fun getItemCount(): Int = reviews.size
}




