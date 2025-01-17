package com.example.reviewr.CommentAdapter



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.reviewr.R

class CommentAdapter(private val comments: List<Map<String, Any>>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.commentTitle)
        val description: TextView = itemView.findViewById(R.id.commentDescription)
        val username: TextView = itemView.findViewById(R.id.commentUsername)
        val time: TextView = itemView.findViewById(R.id.commentTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.title.text = comment["title"] as? String ?: "No Title"
        holder.description.text = comment["description"] as? String ?: "No Description"
        holder.username.text = comment["username"] as? String ?: "Anonymous"

        val timestamp = comment["timestamp"] as? com.google.firebase.Timestamp
        holder.time.text = timestamp?.toDate()?.toString() ?: "Unknown Time"
    }



    override fun getItemCount(): Int = comments.size
}
