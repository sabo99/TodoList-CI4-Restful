package com.sabo.todolist_ci4_restful.Helper.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sabo.todolist_ci4_restful.Activity.Todo.TodoCallback
import com.sabo.todolist_ci4_restful.Model.Todo
import com.sabo.todolist_ci4_restful.R
import com.squareup.picasso.Picasso

class TodoListAdapter(private val context: Context, private val todoList: List<Todo>) :
    RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val ivImg = v.findViewById(R.id.ivImage) as ImageView
        val tvTitle = v.findViewById(R.id.tvTitle) as TextView
        val tvDesc = v.findViewById(R.id.tvDesc) as TextView
        val tvDate = v.findViewById(R.id.tvDate) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_todolist, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todo = todoList[position]

        if (todo.image.isNotEmpty()) {
            val img = TodoCallback.getURLImage(todo.image)
            Picasso.get().load(img).placeholder(R.drawable.ic_round_image)
                .placeholder(android.R.drawable.gallery_thumb).into(holder.ivImg)
        }

        holder.tvTitle.text = todo.title
        holder.tvDesc.text = todo.desc

        if (todo.updated_at == null) holder.tvDate.text = todo.created_at
        else holder.tvDate.text = todo.updated_at

        holder.itemView.setOnClickListener {
            TodoCallback.onShowed(context, todo.id)
        }
    }

    override fun getItemCount(): Int {
        return todoList.size
    }
}