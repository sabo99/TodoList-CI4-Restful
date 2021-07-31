package com.sabo.todolist_ci4_restful.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: Int,
    var username: String,
    var email: String,
    var password: String?,
    var avatar: String?,
    var two_factor_auth: Int,
    var status: Int
) : Parcelable
