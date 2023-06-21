package com.example.taskjob.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskjob.R
import com.example.taskjob.data.User

class UserAdapter(
    private val userList: List<User>,
    private val listener: OnUserItemClickListener
) : RecyclerView.Adapter<UserAdapter.MyViewHolder>() {

    interface OnUserItemClickListener {
        fun onEditButtonClick(user: User)
        fun onDeleteButtonClick(user: User)
        fun onUserItemClick(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = userList[position]
        // Set the user's name and icon
        holder.name.text = user.name
        // Set click listeners for the edit and delete buttons
        holder.editButton.setOnClickListener { listener.onEditButtonClick(user) }
        holder.deleteButton.setOnClickListener { listener.onDeleteButtonClick(user) }
        holder.lyView.setOnClickListener {
            listener.onUserItemClick(user)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val lyView:LinearLayout=view.findViewById(R.id.lyView)
        val name: TextView = view.findViewById(R.id.name)
        val email: TextView = view.findViewById(R.id.email)
        val editButton: ImageButton = view.findViewById(R.id.editButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }
}
