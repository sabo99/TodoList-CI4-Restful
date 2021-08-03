package com.sabo.todolist_ci4_restful.Activity.Profile


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.sabo.todolist_ci4_restful.Activity.Auth.Login
import com.sabo.todolist_ci4_restful.Activity.Profile.Edit.*
import com.sabo.todolist_ci4_restful.Helper.Callback.EventOnRefresh
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Helper.SharedPreference.ManagerPreferences
import com.sabo.todolist_ci4_restful.Model.User
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIResponse
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIService
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileCallback {

    companion object {

        private lateinit var sweetAlertDialogMain: SweetAlertDialog

        private const val contentLoadingEmail = "Update Email"
        private const val contentLoadingUsername = "Update Username"
        private const val contentLoadingPassword = "Update Password"
        private const val contentLoadingTwoFactorAuthEnable = "Enable Two-Factor Authentication"
        private const val contentLoadingTwoFactorAuthDisable = "Disable Two-Factor Authentication"

        private const val contentSuccessEmail = "Email successfully updated"
        private const val contentSuccessUsername = "Username successfully updated"
        private const val contentSuccessPassword = "Password successfully updated"
        private const val contentSuccessTwoFactorAuthEnable = "Two-Factor Authentication 'Enabled'"
        private const val contentSuccessTwoFactorAuthDisable =
            "Two-Factor Authentication 'Disabled'"

        const val KEY_PROFILE = 0
        const val KEY_USERNAME = 1
        const val KEY_EMAIL = 2
        const val KEY_PASSWORD = 3
        const val KEY_TWO_FACTOR_AUTH = 4


        /** Get URL Avatar User */
        fun getURLAvatar(avatar: String): String {
            return RestfulAPIService.AVATAR_TODO_URL + avatar
        }

        /**
         * On Edited
         */
        fun onEdited(context: Context, user: User, keyUpdate: Int){
            ManagerCallback.onStartSweetLoading(context, "")
            RestfulAPIService.requestMethod().editUser(user.uid).enqueue(object :
                Callback<RestfulAPIResponse> {
                override fun onResponse(
                    call: Call<RestfulAPIResponse>,
                    response: Response<RestfulAPIResponse>
                ) {
                    if (response.isSuccessful){
                        ManagerCallback.onStopSweetLoading()
                        val userValue = response.body()!!.user
                        when(keyUpdate){
                            KEY_PROFILE -> context.startActivity(Intent(context, EditProfile::class.java).putExtra("user", userValue))
                            KEY_USERNAME -> EditUsername.onUpdated(context, userValue)
                            KEY_EMAIL -> EditEmail.onUpdated(context, userValue)
                            KEY_PASSWORD -> EditPassword.onUpdated(context, userValue)
                            KEY_TWO_FACTOR_AUTH -> TwoFactorAuth.onUpdated(context, userValue)
                        }
                    }else
                        ManagerCallback.onFailureSweetLoading(response.message())
                }

                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                    ManagerCallback.onFailureSweetLoading("Something wrong with server connection")
                }
            })

        }


        /** On Update User */
        fun onUpdateValues(
            context: Context,
            sweetAlertDialog: SweetAlertDialog,
            user: User,
            keyUpdate: Int
        ) {
            var content = ""
            when (keyUpdate) {
                KEY_USERNAME -> content = contentLoadingUsername
                KEY_EMAIL -> content = contentLoadingEmail
                KEY_PASSWORD -> content = contentLoadingPassword
                KEY_TWO_FACTOR_AUTH -> {
                    when (user.two_factor_auth) {
                        0 -> content = contentLoadingTwoFactorAuthDisable
                        1 -> content = contentLoadingTwoFactorAuthEnable
                    }
                }
            }
            ManagerCallback.onStartSweetLoading(context, content)

            Handler().postDelayed({
                RestfulAPIService.requestMethod()
                    .updateUser(
                        user.uid,
                        user.username,
                        user.email,
                        user.password!!,
                        user.two_factor_auth
                    )
                    .enqueue(
                        object : Callback<RestfulAPIResponse> {
                            override fun onResponse(
                                call: Call<RestfulAPIResponse>,
                                response: Response<RestfulAPIResponse>
                            ) {
                                if (response.isSuccessful) {
                                    when (response.body()!!.code) {
                                        200 -> {
                                            var message = ""
                                            when (keyUpdate) {
                                                KEY_USERNAME -> message = contentSuccessUsername
                                                KEY_EMAIL -> message = contentSuccessEmail
                                                KEY_PASSWORD -> message = contentSuccessPassword
                                                KEY_TWO_FACTOR_AUTH -> {
                                                    when (user.two_factor_auth) {
                                                        0 -> message =
                                                            contentSuccessTwoFactorAuthDisable
                                                        1 -> message =
                                                            contentSuccessTwoFactorAuthEnable
                                                    }
                                                }
                                            }

                                            ManagerCallback.onSuccessSweetLoading(message)
                                            EventBus.getDefault()
                                                .postSticky(EventOnRefresh(true, null))
                                            sweetAlertDialog.dismissWithAnimation()
                                        }

                                        400 -> {
                                            val errors = response.body()!!.errorValidation
                                            var error = ""
                                            when (keyUpdate) {
                                                KEY_USERNAME -> error = errors.username
                                                KEY_EMAIL -> error = errors.email
                                                KEY_PASSWORD -> error = errors.password
                                            }
                                            ManagerCallback.onFailureSweetLoading(error)
                                        }
                                    }
                                } else
                                    ManagerCallback.onFailureSweetLoading(response.message())

                                ManagerCallback.onLog("updateUser", "$response", "${response.body()}")
                            }

                            override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                                ManagerCallback.onFailureSweetLoading("Can't Update User.\nSomething wrong with server connection")
                                ManagerCallback.onLog("updateUser", "${t.message}")
                            }
                        })
            }, 2000)
        }



        /**
         * Delete Account
         */
        fun onDeleteAccount(context: Context) {
            sweetAlertDialogMain = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
            sweetAlertDialogMain.titleText = "Delete Account"
            sweetAlertDialogMain.contentText = "Are you sure delete your account?"
            sweetAlertDialogMain.isShowCancelButton
            sweetAlertDialogMain.cancelText = "Cancel"
            sweetAlertDialogMain.confirmText = "Delete"
            sweetAlertDialogMain.setCancelClickListener {
                sweetAlertDialogMain.dismissWithAnimation()
            }
            sweetAlertDialogMain.setConfirmClickListener {
                val uid = ManagerPreferences.getUID(context)
                ManagerCallback.onStartSweetLoading(context, "Delete account")

                RestfulAPIService.requestMethod().deleteAllTodo(uid).enqueue(object :
                    Callback<RestfulAPIResponse> {
                    override fun onResponse(
                        call: Call<RestfulAPIResponse>,
                        response: Response<RestfulAPIResponse>
                    ) {
                        if (response.isSuccessful) {

                            RestfulAPIService.requestMethod().deleteUser(uid)
                                .enqueue(
                                    object : Callback<RestfulAPIResponse> {
                                        override fun onResponse(
                                            call: Call<RestfulAPIResponse>,
                                            response: Response<RestfulAPIResponse>
                                        ) {
                                            if (response.isSuccessful) {
                                                sweetAlertDialogMain.dismiss()
                                                ManagerCallback.onStopSweetLoading()

                                                ManagerPreferences.clearUserPreferences(context)
                                                context.startActivity(
                                                    Intent(
                                                        context,
                                                        Login::class.java
                                                    )
                                                )
                                                (context as Activity).finish()
                                            } else
                                                ManagerCallback.onFailureSweetLoading(response.message())
                                        }

                                        override fun onFailure(
                                            call: Call<RestfulAPIResponse>,
                                            t: Throwable
                                        ) {
                                            ManagerCallback.onFailureSweetLoading(t.message!!)
                                        }
                                    })

                        } else {
                            ManagerCallback.onFailureSweetLoading(response.message())
                        }
                        ManagerCallback.onLog("deleteAllTodo", "$response", "${response.body()}")
                    }

                    override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                        ManagerCallback.onFailureSweetLoading(t.message!!)
                        Log.d("deleteAllTodoList", t.message!!)
                    }
                })
            }
            sweetAlertDialogMain.show()

            ManagerCallback.initCustomSweetAlertDialog(context, null, sweetAlertDialogMain)
        }


        /**
         * Log Out
         */
        fun onLogout(context: Context) {
            sweetAlertDialogMain = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
            sweetAlertDialogMain.titleText = "Log Out"
            sweetAlertDialogMain.contentText = "Are you sure you want to logout?"
            sweetAlertDialogMain.isShowCancelButton
            sweetAlertDialogMain.confirmText = "Log Out"
            sweetAlertDialogMain.cancelText = "Cancel"
            sweetAlertDialogMain.setCancelClickListener {
                sweetAlertDialogMain.dismissWithAnimation()
            }
            sweetAlertDialogMain.setConfirmClickListener {
                sweetAlertDialogMain.dismissWithAnimation()

                ManagerPreferences.clearUserPreferences(context)
                val i = Intent(context, Login::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(i)
                (context as Activity).finish()
            }

            sweetAlertDialogMain.show()

            ManagerCallback.initCustomSweetAlertDialog(context, null, sweetAlertDialogMain)
        }
    }


}