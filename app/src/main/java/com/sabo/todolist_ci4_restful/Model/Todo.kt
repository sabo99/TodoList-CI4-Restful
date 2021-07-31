package com.sabo.todolist_ci4_restful.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Todo(
    val id: Int,
    val uid: Int,
    var title: String,
    var desc: String,
    var image: String,
    val created_at: String,
    var updated_at: String?
): Parcelable
