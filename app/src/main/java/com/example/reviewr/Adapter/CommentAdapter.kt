package com.example.reviewr.CommentAdapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.reviewr.R

class CommentAdapter(
    private val comments: List<Map<String, Any>>,
    private val onEditClicked: ((Map<String, Any>) -> Unit)? = null,
    private val onDeleteClicked: ((String) -> Unit)? = null
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.commentTitle)
        val description: TextView = itemView.findViewById(R.id.commentDescription)
        val username: TextView = itemView.findViewById(R.id.commentUsername)
        val time: TextView = itemView.findViewById(R.id.commentTime)
        val editButton: Button? = itemView.findViewById(R.id.commentEditButton)
        val deleteButton: Button? = itemView.findViewById(R.id.commentDeleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        Log.d("CommentAdapter", "Binding comment: ${comment["commentId"]}")

        holder.title.text = comment["title"] as? String ?: "No Title"
        holder.description.text = comment["description"] as? String ?: "No Description"
        holder.username.text = comment["username"] as? String ?: "Anonymous"
        val timestamp = comment["timestamp"] as? com.google.firebase.Timestamp
        holder.time.text = timestamp?.toDate()?.toString() ?: "Unknown Time"



        holder.editButton?.apply {
            visibility = if (onEditClicked != null) View.VISIBLE else View.GONE
            setOnClickListener {
                Log.d("CommentAdapter", "Edit button clicked for comment: ${comment["commentId"]}")
                onEditClicked?.invoke(comment)
            }
        }

        holder.deleteButton?.apply {
            visibility = if (onDeleteClicked != null) View.VISIBLE else View.GONE
            setOnClickListener {
                Log.d("CommentAdapter", "Delete button clicked for comment: ${comment["commentId"]}")
                val commentId = comment["commentId"] as? String ?: return@setOnClickListener
                onDeleteClicked?.invoke(commentId)
            }
        }
    }

    override fun getItemCount(): Int = comments.size
}
