package com.example.videocall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videocall.model.User
import com.example.videocall.R

class ActiveUsersAdapter(onItemClickListener: (User) -> Unit) :
    RecyclerView.Adapter<ActiveUsersAdapter.ActiveUserViewHolder>() {
    private val data = ArrayList<User>()
    private val onItemClicked: (Int) -> Unit = {
        onItemClickListener(data[it])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveUserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_active_user, parent, false)
        return ActiveUserViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(holder: ActiveUserViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun addItem(activeUser: User) {
        data.add(activeUser)
        notifyItemInserted(data.size - 1)
    }

    fun removeItem(activeUserID: String) {
        for (i in 0 until data.size) {
            if (data[i].id == activeUserID) {
                data.removeAt(i)
                notifyItemRemoved(i)
                break
            }
        }
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    class ActiveUserViewHolder(itemView: View, private val clickedPositionListener: (Int) -> Unit) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val name: TextView = itemView.findViewById(R.id.activeUserNameTxt)

        fun bind(activeUser: User) {
            itemView.setOnClickListener(this)
            name.text = activeUser.name
        }

        override fun onClick(v: View) {
            clickedPositionListener(adapterPosition)
        }
    }
}