package com.sabo.todolist_ci4_restful.Model

data class LogUsers(
    val log_id: Int,
    val uid: Int,
    val mac_address: String,
    val action: String,
    val created_at: String,
)
