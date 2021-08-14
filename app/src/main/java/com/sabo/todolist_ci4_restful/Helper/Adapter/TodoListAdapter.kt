package com.sabo.todolist_ci4_restful.Helper.Adapter

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sabo.todolist_ci4_restful.Activity.Todo.TodoCallback
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Model.Todo
import com.sabo.todolist_ci4_restful.R

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

        Glide.with(context).load(R.drawable.loading).into(holder.ivImg)
        Handler().postDelayed({
            Glide.with(context)
                .load(ManagerCallback.getURLImage(todo.image))
                .error(R.drawable.ic_round_image_not_supported_240)
                .into(holder.ivImg)
        }, 1000)

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