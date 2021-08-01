package com.sabo.todolist_ci4_restful.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.sabo.todolist_ci4_restful.Activity.Auth.Login
import com.sabo.todolist_ci4_restful.Activity.Profile.Profile
import com.sabo.todolist_ci4_restful.Activity.Todo.TodoCallback
import com.sabo.todolist_ci4_restful.Helper.Adapter.TodoListAdapter
import com.sabo.todolist_ci4_restful.Helper.Callback.EventOnRefresh
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Helper.SharedPreference.ManagerPreferences
import com.sabo.todolist_ci4_restful.Model.User
import com.sabo.todolist_ci4_restful.R
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIResponse
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIService
import com.sabo.todolist_ci4_restful.databinding.ActivityMainBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var user: User
    private var uid = 0
    private var isEnableMenuItem = false
    private var isVisibleMenuItem = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

        binding.swipeRefresh.setOnRefreshListener {
            binding.lottieEmptyTodoList.visibility = View.GONE
            binding.lottieInternalServerError.visibility = View.GONE
            binding.lottieNoInternetAccess.visibility = View.GONE

            binding.shimmerLayout.visibility = View.VISIBLE
            binding.rvTodoList.visibility = View.GONE
            binding.shimmerLayout.startShimmer()

            binding.swipeRefresh.isRefreshing = true

            Handler().postDelayed({
                binding.swipeRefresh.isRefreshing = false
                loadTodoList()
            }, 2000)
        }

        binding.fabAdd.setOnClickListener {
            if (checkEnableMenu("Cannot create new todo"))
                TodoCallback.onCreated(this, user)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home, menu)
        menu!!.findItem(R.id.action_profile).isVisible = isVisibleMenuItem

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_profile)
            if (checkEnableMenu("Cannot open your profile"))
                startActivity(
                    Intent(
                        this,
                        Profile::class.java
                    )
                )
        return super.onOptionsItemSelected(item)
    }

    private fun checkEnableMenu(message: String): Boolean {
        if (!isEnableMenuItem) {
            ManagerCallback.onSweetAlertDialogWarning(
                this,
                "$message.\nSomething wrong with server connection",
            )
            return false
        }
        return true
    }

    private fun initViews() {
        uid = ManagerPreferences.getUID(this)

        binding.rvTodoList.layoutManager = LinearLayoutManager(this)

        binding.fabAdd.hide()
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.rvTodoList.visibility = View.GONE
        binding.shimmerLayout.startShimmer()

        Handler().postDelayed({ loadTodoList() }, 500)
    }


    private fun loadTodoList() {
        RestfulAPIService.requestMethod().showTodo(uid)
            .enqueue(object : Callback<RestfulAPIResponse> {
                override fun onResponse(
                    call: Call<RestfulAPIResponse>,
                    response: Response<RestfulAPIResponse>
                ) {
                    if (response.isSuccessful) {
                        if (response.body()!!.todoList.isNotEmpty()) {
                            binding.rvTodoList.adapter =
                                TodoListAdapter(this@MainActivity, response.body()!!.todoList)
                            binding.rvTodoList.visibility = View.VISIBLE
                            binding.lottieEmptyTodoList.visibility = View.GONE
                            binding.lottieInternalServerError.visibility = View.GONE
                            binding.lottieNoInternetAccess.visibility = View.GONE
                        } else {
                            binding.rvTodoList.visibility = View.GONE
                            binding.lottieEmptyTodoList.visibility = View.VISIBLE
                            binding.lottieInternalServerError.visibility = View.GONE
                            binding.lottieNoInternetAccess.visibility = View.GONE
                        }

                    } else {
                        /** Internal Server Error (500)
                         * MySQL Shutdown
                         */
                        binding.rvTodoList.visibility = View.GONE
                        binding.lottieEmptyTodoList.visibility = View.GONE
                        binding.lottieInternalServerError.visibility = View.VISIBLE
                        binding.lottieNoInternetAccess.visibility = View.GONE
                    }
                    binding.shimmerLayout.visibility = View.GONE
                    binding.fabAdd.show()

                    ManagerCallback.onLog("showTodo", "$response", "${response.body()}")
                }

                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                    binding.shimmerLayout.visibility = View.GONE
                    binding.fabAdd.show()

                    if (t.message!!.contains("after 10000ms")) {
                        /** Loss Connection
                         * Apache Shutdown
                         */
                        binding.rvTodoList.visibility = View.GONE
                        binding.lottieEmptyTodoList.visibility = View.GONE
                        binding.lottieInternalServerError.visibility = View.VISIBLE
                        binding.lottieNoInternetAccess.visibility = View.GONE
                    } else {
                        /** Loss Connection
                         * No Internet Access
                         */
                        binding.rvTodoList.visibility = View.GONE
                        binding.lottieEmptyTodoList.visibility = View.GONE
                        binding.lottieInternalServerError.visibility = View.GONE
                        binding.lottieNoInternetAccess.visibility = View.VISIBLE
                    }

                    ManagerCallback.onLog("showTodo", "${t.message}")
                }
            })

        RestfulAPIService.requestMethod().showUser(uid).enqueue(object :
            Callback<RestfulAPIResponse> {
            override fun onResponse(
                call: Call<RestfulAPIResponse>,
                response: Response<RestfulAPIResponse>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.user != null)
                        user = response.body()!!.user
                    else {
                        val sweet =
                            SweetAlertDialog(this@MainActivity, SweetAlertDialog.WARNING_TYPE)
                        sweet.titleText = "Oops!"
                        sweet.contentText = "Your account has been deleted from the database."
                        sweet.setConfirmClickListener {
                            ManagerPreferences.clearUserPreferences(this@MainActivity)
                            startActivity(Intent(this@MainActivity, Login::class.java))
                            finish()
                        }
                        sweet.show()
                        ManagerCallback.initCustomSweetAlertDialog(this@MainActivity, null, sweet)
                    }
                }
                isEnableMenuItem = true
                isVisibleMenuItem = true
                invalidateOptionsMenu()
                ManagerCallback.onLog("showUser", "$response", "${response.body()}")
            }

            override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                if (t.message!!.contains("failed to connect")) {
                    isEnableMenuItem = false
                    isVisibleMenuItem = true
                    invalidateOptionsMenu()
                }
                ManagerCallback.onLog("showUser", "${t.message}")
            }
        })
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun eventOnRefresh(event: EventOnRefresh) {
        if (event.onRefresh) {
            initViews()
            event.onRefresh = false
        }
    }
}