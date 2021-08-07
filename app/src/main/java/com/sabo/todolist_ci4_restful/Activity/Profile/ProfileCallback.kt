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
import com.sabo.todolist_ci4_restful.Helper.Callback.KeyStore
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

        /**
         * On Edited
         */
        fun onEdited(context: Context, user: User, keyUpdate: Int) {
            ManagerCallback.onStartSweetLoading(context, "")
            RestfulAPIService.requestMethod().editUser(user.uid).enqueue(object :
                Callback<RestfulAPIResponse> {
                override fun onResponse(
                    call: Call<RestfulAPIResponse>,
                    response: Response<RestfulAPIResponse>
                ) {
                    when(response.code()){
                        200 -> {
                            ManagerCallback.onStopSweetLoading()
                            val userValue = response.body()!!.user
                            when (keyUpdate) {
                                KeyStore.KEY_PROFILE -> {
                                    context.startActivity(
                                        Intent(
                                            context,
                                            EditProfile::class.java
                                        ).putExtra("user", userValue)
                                    )
                                    ManagerCallback.onCreateLogUser(
                                        response.body()!!.user.uid,
                                        KeyStore.EDIT_PROFILE
                                    )
                                }
                                KeyStore.KEY_USERNAME -> {
                                    EditUsername.onUpdated(context, userValue)
                                    ManagerCallback.onCreateLogUser(
                                        response.body()!!.user.uid,
                                        KeyStore.EDIT_USERNAME
                                    )
                                }
                                KeyStore.KEY_EMAIL -> {
                                    EditEmail.onUpdated(context, userValue)
                                    ManagerCallback.onCreateLogUser(
                                        response.body()!!.user.uid,
                                        KeyStore.EDIT_EMAIL
                                    )
                                }
                                KeyStore.KEY_PASSWORD -> {
                                    EditPassword.onUpdated(context, userValue)
                                    ManagerCallback.onCreateLogUser(
                                        response.body()!!.user.uid,
                                        KeyStore.EDIT_PASSWORD
                                    )
                                }
                                KeyStore.KEY_TWO_FACTOR_AUTH -> TwoFactorAuth.onUpdated(
                                    context,
                                    userValue
                                )
                            }
                        }
                        404 -> ManagerCallback.onFailureSweetLoading(response.message())
                        500 -> ManagerCallback.onFailureSweetLoading(response.message())
                    }

                    ManagerCallback.onLog("editUser", response)
                }

                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                    ManagerCallback.onFailureSweetLoading(KeyStore.ON_FAILURE)
                    ManagerCallback.onLog("editUser", "${t.message}")
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
                KeyStore.KEY_USERNAME -> content = KeyStore.contentLoadingUsername
                KeyStore.KEY_EMAIL -> content = KeyStore.contentLoadingEmail
                KeyStore.KEY_PASSWORD -> content = KeyStore.contentLoadingPassword
                KeyStore.KEY_TWO_FACTOR_AUTH -> {
                    when (user.two_factor_auth) {
                        0 -> content = KeyStore.contentLoadingTwoFactorAuthDisable
                        1 -> content = KeyStore.contentLoadingTwoFactorAuthEnable
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
                                when (response.code()) {
                                    200 -> {
                                        var message = ""
                                        when (keyUpdate) {
                                            KeyStore.KEY_USERNAME -> {
                                                message = KeyStore.contentSuccessUsername
                                                ManagerCallback.onCreateLogUser(
                                                    user.uid,
                                                    KeyStore.UPDATE_USERNAME
                                                )
                                            }
                                            KeyStore.KEY_EMAIL -> {
                                                message = KeyStore.contentSuccessEmail
                                                ManagerCallback.sendMailSuccessChangeEmailAddress(
                                                    user
                                                )
                                                ManagerCallback.onCreateLogUser(
                                                    user.uid,
                                                    KeyStore.UPDATE_EMAIL
                                                )
                                            }
                                            KeyStore.KEY_PASSWORD -> {
                                                message = KeyStore.contentSuccessPassword
                                                ManagerCallback.onCreateLogUser(
                                                    user.uid,
                                                    KeyStore.UPDATE_PASSWORD
                                                )
                                            }
                                            KeyStore.KEY_TWO_FACTOR_AUTH -> {
                                                when (user.two_factor_auth) {
                                                    0 -> {
                                                        message =
                                                            KeyStore.contentSuccessTwoFactorAuthDisable
                                                        ManagerCallback.onCreateLogUser(
                                                            user.uid,
                                                            KeyStore.DISABLE_TWO_FACTOR_AUTH
                                                        )
                                                    }
                                                    1 -> {
                                                        message =
                                                            KeyStore.contentSuccessTwoFactorAuthEnable
                                                        ManagerCallback.onCreateLogUser(
                                                            user.uid,
                                                            KeyStore.ENABLE_TWO_FACTOR_AUTH
                                                        )
                                                    }
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
                                            KeyStore.KEY_USERNAME -> error = errors.username
                                            KeyStore.KEY_EMAIL -> error = errors.email
                                            KeyStore.KEY_PASSWORD -> error = errors.password
                                        }
                                        ManagerCallback.onFailureSweetLoading(error)
                                    }
                                    500 -> ManagerCallback.onSweetAlertDialogWarning(
                                        context,
                                        response.message()
                                    )
                                }
                                ManagerCallback.onLog("updateUser", response)
                            }

                            override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                                ManagerCallback.onFailureSweetLoading("Can't Update User.\n${KeyStore.ON_FAILURE}")
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
                            ManagerCallback.onCreateLogUser(uid, KeyStore.DELETE_ALL_TODO)

                            RestfulAPIService.requestMethod().deleteUser(uid)
                                .enqueue(
                                    object : Callback<RestfulAPIResponse> {
                                        override fun onResponse(
                                            call: Call<RestfulAPIResponse>,
                                            response: Response<RestfulAPIResponse>
                                        ) {
                                            when(response.code()){
                                                200 -> {
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
                                                    ManagerCallback.onCreateLogUser(
                                                        uid,
                                                        KeyStore.DELETE_ACCOUNT
                                                    )
                                                }
                                                500 -> {
                                                    ManagerCallback.onFailureSweetLoading(response.message())
                                                }
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<RestfulAPIResponse>,
                                            t: Throwable
                                        ) {
                                            ManagerCallback.onFailureSweetLoading(KeyStore.ON_FAILURE)
                                        }
                                    })

                        } else {
                            ManagerCallback.onFailureSweetLoading(response.message())
                        }
                        ManagerCallback.onLog("deleteAllTodo", response)
                    }

                    override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                        ManagerCallback.onFailureSweetLoading(t.message!!)
                        Log.d("deleteAllTodoList", t.message!!)
                    }
                })
            }
            sweetAlertDialogMain.show()

            ManagerCallback.initCustomSweetAlertDialog(context, sweetAlertDialogMain)
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

                ManagerCallback.onCreateLogUser(
                    ManagerPreferences.getUID(context),
                    KeyStore.LOG_OUT
                )
                ManagerPreferences.clearUserPreferences(context)
                val i = Intent(context, Login::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(i)
                (context as Activity).finish()
            }

            sweetAlertDialogMain.show()

            ManagerCallback.initCustomSweetAlertDialog(context, sweetAlertDialogMain)
        }
    }


}