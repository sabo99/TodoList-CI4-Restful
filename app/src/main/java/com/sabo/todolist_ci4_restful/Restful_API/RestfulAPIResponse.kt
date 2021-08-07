package com.sabo.todolist_ci4_restful.Restful_API

import com.google.gson.annotations.SerializedName
import com.sabo.todolist_ci4_restful.Model.ErrorValidation
import com.sabo.todolist_ci4_restful.Model.LogUsers
import com.sabo.todolist_ci4_restful.Model.Todo
import com.sabo.todolist_ci4_restful.Model.User

data class RestfulAPIResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("errorValidation") val errorValidation: ErrorValidation,

    @SerializedName("user") val user: User,
    @SerializedName("todoList") val todoList: List<Todo>,
    @SerializedName("todo") val todo: Todo,
    @SerializedName("logUsers") val logUsers: LogUsers
)