package com.sabo.todolist_ci4_restful.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ErrorValidation(
    var uid: Int,
    var username: String,
    var email: String,
    var password: String,
    var password_confirm: String,
    var avatar: String,
    var id: Int,
    var title: String,
    var desc: String,
    var image: String
): Parcelable
