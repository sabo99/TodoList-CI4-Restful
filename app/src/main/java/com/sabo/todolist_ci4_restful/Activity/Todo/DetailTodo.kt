package com.sabo.todolist_ci4_restful.Activity.Todo

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.sabo.todolist_ci4_restful.Helper.Callback.EventOnRefresh
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Helper.SharedPreference.ManagerPreferences
import com.sabo.todolist_ci4_restful.Model.Todo
import com.sabo.todolist_ci4_restful.R
import com.sabo.todolist_ci4_restful.databinding.ActivityDetailTodoBinding
import com.squareup.picasso.Picasso
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DetailTodo : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTodoBinding
    private lateinit var todo: Todo
    private var uid = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.todo, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun finish() {
        super.finish()
        EventBus.getDefault().postSticky(EventOnRefresh(true, null))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.action_edit -> TodoCallback.onEdited(this, todo)
            R.id.action_delete -> TodoCallback.onDeleted(this, todo)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initViews() {
        supportActionBar!!.title = "Detail Todo"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        uid = ManagerPreferences.getUID(this)
        todo = intent.getParcelableExtra("todo")

        Picasso.get().load(ManagerCallback.getURLImage(todo.image)).into(binding.ivImage)
        binding.tvTitle.text = todo.title
        binding.tvDesc.text = todo.desc
        if (todo.updated_at == null)
            binding.tvDate.text = "Last update ${todo.created_at}"
        else
            binding.tvDate.text = "Last update ${todo.updated_at}"
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        ManagerCallback.onStopCheckSelfMacAddress()
    }

    override fun onResume() {
        super.onResume()
        ManagerCallback.onStartCheckSelfMACAddress(this)
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(event: EventOnRefresh){
        if (event.onRefresh){

            todo = event.value as Todo
            Picasso.get().load(ManagerCallback.getURLImage(todo.image)).into(binding.ivImage)
            binding.tvTitle.text = todo.title
            binding.tvDesc.text = todo.desc

            event.onRefresh = false
        }
    }
}