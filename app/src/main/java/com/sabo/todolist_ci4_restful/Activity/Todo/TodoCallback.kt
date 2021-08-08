package com.sabo.todolist_ci4_restful.Activity.Todo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.widget.Toast
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.sabo.todolist_ci4_restful.Helper.Callback.EventOnRefresh
import com.sabo.todolist_ci4_restful.Helper.Callback.KeyStore
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Model.Todo
import com.sabo.todolist_ci4_restful.Model.User
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIResponse
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIService
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TodoCallback {
    companion object {

        fun onCreated(context: Context, user: User) {
            context.startActivity(Intent(context, CreateTodo::class.java).putExtra("user", user))
        }

        fun onShowed(context: Context, id: Int) {
            ManagerCallback.onStartSweetLoading(
                context,
                "Load todo id : ${ManagerCallback.onHashNumber(id)}"
            )
            RestfulAPIService.requestMethod().editTodo(id).enqueue(object :
                Callback<RestfulAPIResponse> {
                override fun onResponse(
                    call: Call<RestfulAPIResponse>,
                    response: Response<RestfulAPIResponse>
                ) {
                    when(response.code()){
                        200 -> {
                            val todo = response.body()!!.todo

                            Handler().postDelayed({
                                ManagerCallback.onStopSweetLoading()

                                context.startActivity(
                                    Intent(context, DetailTodo::class.java)
                                        .putExtra("todo", todo)
                                )
                            }, 2000)
                        }
                        404 -> ManagerCallback.onFailureSweetLoading(response.message())
                        500 -> ManagerCallback.onFailureSweetLoading(ManagerCallback.getErrorBody(response)!!.message)
                    }
                    ManagerCallback.onLog("editTodo", response)
                }

                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                    ManagerCallback.onFailureSweetLoading(KeyStore.ON_FAILURE)
                    ManagerCallback.onLog("editTodo", "${t.message}")
                }
            })
        }

        fun onEdited(context: Context, todo: Todo) {
            context.startActivity(
                Intent(context, EditTodo::class.java)
                    .putExtra("todo", todo)
            )
            ManagerCallback.onCreateLogUser(todo.uid, KeyStore.EDIT_TODO)
        }

        fun onDeleted(context: Context, todo: Todo) {
            val sweet = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
            sweet.titleText = "Delete Todo"
            sweet.contentText = "Are you sure delete this todo?"
            sweet.isShowCancelButton
            sweet.confirmText = "Delete"
            sweet.cancelText = "Cancel"
            sweet.setCancelClickListener { sweet.dismissWithAnimation() }
            sweet.setConfirmClickListener {
                sweet.dismissWithAnimation()
                ManagerCallback.onStartSweetLoading(context, "Delete todo")

                RestfulAPIService.requestMethod().deleteTodo(todo.id)
                    .enqueue(object : Callback<RestfulAPIResponse> {
                        override fun onResponse(
                            call: Call<RestfulAPIResponse>,
                            response: Response<RestfulAPIResponse>
                        ) {
                            if (response.isSuccessful) {
                                Handler().postDelayed({

                                    ManagerCallback.onStopSweetLoading()
                                    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()
                                    (context as Activity).finish()
                                    EventBus.getDefault().postSticky(EventOnRefresh(true, null))
                                }, 2000)
                                ManagerCallback.onCreateLogUser(todo.uid, KeyStore.DELETE_TODO)
                            } else
                                ManagerCallback.onFailureSweetLoading(response.message())
                            ManagerCallback.onLog("deleteTodo", response)
                        }

                        override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                            ManagerCallback.onFailureSweetLoading("Can't Delete Todo.\n${KeyStore.ON_FAILURE}")
                            ManagerCallback.onLog("deleteTodo", "${t.message}")
                        }

                    })
            }
            sweet.show()
            ManagerCallback.initCustomSweetAlertDialog(context, sweet)
        }

        fun onFinish(context: Context, sweetAlertDialog: SweetAlertDialog) {
            sweetAlertDialog.dismissWithAnimation()
            (context as Activity).finish()
        }
    }

}