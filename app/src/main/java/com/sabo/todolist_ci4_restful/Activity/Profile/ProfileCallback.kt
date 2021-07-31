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


        fun getURLAvatar(avatar: String): String {
            return RestfulAPIService.AVATAR_TODO_URL + avatar
        }

        /**
         * OnUpdate
         */
        fun onEdited(context: Context, user: User, keyUpdate: Int){
            ManagerCallback.onStartSweetLoading(context, "Please wait", "")
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
            ManagerCallback.onStartSweetLoading(context, "Please wait", content)

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
                                                .postSticky(EventOnRefresh(true, ""))
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

                                Log.d("updateUser", response.body()!!.toString())
                            }

                            override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                                ManagerCallback.onFailureSweetLoading("Can't Update User.\nSomething wrong with server connection")
                                Log.d("updateUser", t.message!!)
                            }
                        })
            }, 2000)


        }


        /**
         * ===================
         * Below is Deprecated
         * ===================
         */
        fun deprecated() {}

//
//        /**
//         * Update New Username
//         */
//        fun onUpdateUsername(context: Context, user: User, view: View) {
//            sweetAlertDialogMain = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
//            bindingUsername = SweetAlertDialogEditUsernameBinding.bind(view)
//
//            sweetAlertDialogMain.showCancelButton(true)
//            sweetAlertDialogMain.cancelText = "Cancel"
//            sweetAlertDialogMain.confirmText = "Done"
//            sweetAlertDialogMain.setOnShowListener {
//                bindingUsername.ibClose.setOnClickListener {
//                    sweetAlertDialogMain.dismissWithAnimation()
//                    sweetAlertDialogMain.dismiss()
//                }
//
//                bindingUsername.tilUsername.suffixText = ManagerCallback.hashTagNumber(user.uid)
//                bindingUsername.etUsername.setText(user.username)
//                onTextWatcherSweetAlertDialogUsername()
//            }
//            sweetAlertDialogMain.setCancelClickListener {
//                sweetAlertDialogMain.dismissWithAnimation()
//                sweetAlertDialogMain.dismiss()
//            }
//            sweetAlertDialogMain.setConfirmClickListener {
//                val currentPassword = bindingUsername.etCurrentPassword.text.toString()
//                val newUsername = bindingUsername.etUsername.text.toString()
//
//                if (newUsername == user.username)
//                    sweetAlertDialogMain.dismissWithAnimation()
//                else
//                    reAuth(
//                        context,
//                        user,
//                        currentPassword,
//                        newUsername,
//                        KEY_MESSAGE_USERNAME,
//                        KEY_USERNAME
//                    )
//            }
//            sweetAlertDialogMain.show()
//
//            ManagerCallback.initCustomSweetAlertDialog(context, view, sweetAlertDialogMain)
//        }
//
//        /**
//         * ================
//         * UPDATE NEW EMAIL
//         * ================
//         */
//        fun onUpdateEmail(context: Context, user: User, view: View) {
//            sweetAlertDialogMain = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
//            bindingEmail = SweetAlertDialogEditEmailBinding.bind(view)
//
//            sweetAlertDialogMain.isShowCancelButton
//            sweetAlertDialogMain.cancelText = "Cancel"
//            sweetAlertDialogMain.confirmText = "Done"
//            sweetAlertDialogMain.setOnShowListener {
//                bindingEmail.ibClose.setOnClickListener {
//                    sweetAlertDialogMain.dismissWithAnimation()
//                    sweetAlertDialogMain.dismiss()
//                }
//
//
//                onTextWatcherSweetAlertDialogEmail()
//            }
//            sweetAlertDialogMain.setCancelClickListener {
//                sweetAlertDialogMain.dismissWithAnimation()
//                sweetAlertDialogMain.dismiss()
//            }
//            sweetAlertDialogMain.setConfirmClickListener {
//                val currentPassword = bindingEmail.etCurrentPassword.text.toString()
//                val newEmail = bindingEmail.etEmail.text.toString()
//
//                if (newEmail == user.email)
//                    bindingEmail.tilEmail.error = "Email has been used on your account"
//                else
//                    reAuth(
//                        context,
//                        user,
//                        currentPassword,
//                        newEmail,
//                        KEY_MESSAGE_EMAIL,
//                        KEY_EMAIL
//                    )
//            }
//            sweetAlertDialogMain.show()
//
//            ManagerCallback.initCustomSweetAlertDialog(context, view, sweetAlertDialogMain)
//        }
//
//
//        /**
//         * ================
//         * UPDATE NEW PASSWORD
//         * ================
//         */
//        fun onUpdatePassword(context: Context, user: User, view: View) {
//            sweetAlertDialogMain = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
//            bindingPassword = SweetAlertDialogEditPasswordBinding.bind(view)
//
//            sweetAlertDialogMain.isShowCancelButton
//            sweetAlertDialogMain.cancelText = "Cancel"
//            sweetAlertDialogMain.confirmText = "Done"
//            sweetAlertDialogMain.setOnShowListener {
//                bindingPassword.ibClose.setOnClickListener {
//                    sweetAlertDialogMain.dismissWithAnimation()
//                    sweetAlertDialogMain.dismiss()
//                }
//
//                onTextWatcherSweetAlertDialogPassword()
//            }
//            sweetAlertDialogMain.setCancelClickListener {
//                sweetAlertDialogMain.dismissWithAnimation()
//                sweetAlertDialogMain.dismiss()
//            }
//            sweetAlertDialogMain.setConfirmClickListener {
//                val currentPassword = bindingPassword.etCurrentPassword.text.toString()
//                val newPassword = bindingPassword.etNewPassword.text.toString()
//
//                if (newPassword == currentPassword)
//                    bindingPassword.tilNewPassword.error =
//                        "Your new password is still the same as the old password"
//                else
//                    reAuth(
//                        context,
//                        user,
//                        currentPassword,
//                        newPassword,
//                        KEY_MESSAGE_PASSWORD,
//                        KEY_PASSWORD
//                    )
//            }
//            sweetAlertDialogMain.show()
//
//            ManagerCallback.initCustomSweetAlertDialog(context, view, sweetAlertDialogMain)
//        }
//
//
//        /**
//         * =========================
//         * TWO FACTOR AUTH
//         * =========================
//         */
//        fun onEnableTwoFactorAuth(context: Context, user: User, view: View) {
//            sweetAlertDialogMain = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
//            bindingTwoFactorAuth = SweetAlertDialogEnableTwoAuthBinding.bind(view)
//
//            bindingTwoFactorAuth.tilCurrentPassword.visibility = View.VISIBLE
//            bindingTwoFactorAuth.tilVerificationCode.visibility = View.GONE
//            bindingTwoFactorAuth.tvResendCode.visibility = View.GONE
//            bindingTwoFactorAuth.tvSubtitle.text = "Enter your current password."
//
//            sweetAlertDialogMain.isShowCancelButton
//            sweetAlertDialogMain.cancelText = "Cancel"
//            sweetAlertDialogMain.confirmText = "Check"
//            sweetAlertDialogMain.setOnShowListener {
//                bindingTwoFactorAuth.ibClose.setOnClickListener {
//                    sweetAlertDialogMain.dismissWithAnimation()
//                }
//                onTextWatcherSweetAlertDialogTwoFactorAuth()
//            }
//            sweetAlertDialogMain.setCancelClickListener {
//                sweetAlertDialogMain.dismissWithAnimation()
//                sweetAlertDialogMain.dismiss()
//            }
//            sweetAlertDialogMain.setConfirmClickListener {
//                val currentPassword = bindingTwoFactorAuth.etCurrentPassword.text.toString()
//                val twoFactorAuth = if (user.two_factor_auth == 0) 1 else 0
//                reAuth(
//                    context,
//                    user,
//                    currentPassword,
//                    "$twoFactorAuth",
//                    KEY_MESSAGE_TWO_FACTOR_AUTH,
//                    KEY_TWO_FACTOR_AUTH
//                )
//            }
//            sweetAlertDialogMain.show()
//
//            ManagerCallback.initCustomSweetAlertDialog(context, view, sweetAlertDialogMain)
//        }
//
//
//        /**
//         * Re-Authentication
//         */
//        private fun reAuth(
//            context: Context,
//            user: User,
//            currentPassword: String,
//            valueUpdate: String,
//            keyMessage: String,
//            keyUpdate: Int
//        ) {
//            RestfulAPIService.requestMethod().signIn(user.email, currentPassword).enqueue(object :
//                Callback<RestfulAPIResponse> {
//                override fun onResponse(
//                    call: Call<RestfulAPIResponse>,
//                    response: Response<RestfulAPIResponse>
//                ) {
//                    if (response.isSuccessful) {
//                        when (response.body()!!.code) {
//                            200 -> {
//                                ManagerCallback.onStartSweetLoading(
//                                    context,
//                                    "Please wait",
//                                    "$keyMessage"
//                                )
//
//                                Handler().postDelayed({
//                                    when (keyUpdate) {
//                                        KEY_USERNAME -> updateValue(
//                                            user.uid,
//                                            "$valueUpdate",
//                                            user.email,
//                                            currentPassword,
//                                            0,
//                                            KEY_USERNAME
//                                        )
//                                        KEY_EMAIL -> checkEmailExist(
//                                            user,
//                                            "$valueUpdate",
//                                            currentPassword,
//                                            KEY_EMAIL
//                                        )
//                                        KEY_PASSWORD -> updateValue(
//                                            user.uid,
//                                            user.username,
//                                            user.email,
//                                            "$valueUpdate",
//                                            0,
//                                            KEY_PASSWORD
//                                        )
//                                        KEY_TWO_FACTOR_AUTH -> {
//                                            val code = ManagerCallback.generateTokenCode()
//
//                                            changeLayout(
//                                                context,
//                                                user,
//                                                "$valueUpdate",
//                                                currentPassword,
//                                                code,
//                                                KEY_TWO_FACTOR_AUTH
//                                            )
//                                            sendVerificationCode(
//                                                context,
//                                                user,
//                                                "$valueUpdate",
//                                                currentPassword,
//                                                "$code"
//
//                                            )
//                                        }
//                                    }
//                                }, 2000)
//                            }
//
//                            400 -> {
//                                val errors = response.body()!!.errorValidation
//
//                                when (keyUpdate) {
//                                    KEY_USERNAME -> {
//                                        bindingUsername.tilUsername.error = errors.username
//                                        bindingUsername.tilCurrentPassword.error = errors.password
//                                    }
//                                    KEY_EMAIL -> {
//                                        bindingEmail.tilEmail.error = errors.email
//                                        bindingEmail.tilCurrentPassword.error = errors.password
//                                    }
//                                    KEY_PASSWORD -> {
//                                        bindingPassword.tilCurrentPassword.error =
//                                            errors.password
//                                        bindingPassword.tilNewPassword.error = errors.password
//                                    }
//                                    KEY_TWO_FACTOR_AUTH -> {
//                                        bindingTwoFactorAuth.tilCurrentPassword.error =
//                                            errors.password
//                                    }
//                                }
//                            }
//                        }
//                    } else {
//                        if (response.message() == "Not Found") {
//                            val message = "Your password was wrong."
//                            when (keyUpdate) {
//                                KEY_USERNAME -> bindingUsername.tilCurrentPassword.error = message
//                                KEY_EMAIL -> bindingEmail.tilCurrentPassword.error = message
//                                KEY_PASSWORD -> bindingPassword.tilCurrentPassword.error = message
//                                KEY_TWO_FACTOR_AUTH -> bindingTwoFactorAuth.tilCurrentPassword.error =
//                                    message
//                            }
//                        } else {
//                            ManagerCallback.onStartSweetLoading(
//                                context,
//                                "Please wait",
//                                "Update $keyMessage"
//                            )
//                            ManagerCallback.onFailureSweetLoading(response.message())
//                        }
//                        Log.d("reAuth", response.message())
//                    }
//                }
//
//                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
//                    Log.d("reAuth", t.message!!)
//                }
//            })
//        }
//
//        private fun changeLayout(
//            context: Context,
//            user: User,
//            twoFactorAuth: String,
//            currentPassword: String,
//            code: String,
//            keyUpdate: Int
//        ) {
//
//            bindingTwoFactorAuth.tilCurrentPassword.visibility = View.GONE
//            bindingTwoFactorAuth.tilVerificationCode.visibility = View.VISIBLE
//            bindingTwoFactorAuth.tvResendCode.visibility = View.VISIBLE
//
//            if (user.two_factor_auth == 0) {
//                bindingTwoFactorAuth.tvSubtitle.text =
//                    "Enter the verification code to enable two factor auth."
//                sweetAlertDialogMain.confirmText = "Enable"
//            } else {
//                bindingTwoFactorAuth.tvSubtitle.text =
//                    "Enter the verification code to disable two factor auth."
//                sweetAlertDialogMain.confirmText = "Disable"
//            }
//
//            sweetAlertDialogMain.setConfirmClickListener {
//                val verificationCode = bindingTwoFactorAuth.etVerificationCode.text.toString()
//
//                if (verificationCode != code) {
//                    bindingTwoFactorAuth.tilVerificationCode.error =
//                        "Your verification code is wrong."
//                } else {
//                    if (user.two_factor_auth == 0)
//                        ManagerCallback.onStartSweetLoading(
//                            context,
//                            "Please wait",
//                            "Enable two factor authentication"
//                        )
//                    else
//                        ManagerCallback.onStartSweetLoading(
//                            context,
//                            "Please wait",
//                            "Disable two factor authentication"
//                        )
//
//                    updateValue(
//                        user.uid,
//                        user.username,
//                        user.email,
//                        currentPassword,
//                        twoFactorAuth.toInt(),
//                        keyUpdate
//                    )
//                }
//            }
//        }
//
//
//        /**
//         * Send Verification Code to Gmail
//         */
//        private fun sendVerificationCode(
//            context: Context,
//            user: User,
//            twoFactorAuth: String,
//            currentPassword: String,
//            code: String,
//        ) {
//            countDownTimer(context, user, twoFactorAuth, currentPassword, 120000, 1000)
//
//            Thread(Runnable {
//                try {
//                    val sender =
//                        GMailSender(
//                            Credentials.EMAIL_SENDER,
//                            Credentials.PASSWORD_SENDER
//                        )
//                    sender.sendMail(
//                        "Two Factor Authentication Verification Code",
//                        "Code : $code",
//                        "${Credentials.EMAIL_SENDER}",
//                        "${user.email}"
//                    )
//
//                    (context as Activity).runOnUiThread {
//                        ManagerCallback.onSuccessSweetLoading("Mail sent successfully")
//                    }
//
//                } catch (e: Exception) {
//                    Log.d("SendEmail", e.message.toString())
//                }
//            }).start()
//
//        }
//
//        private fun countDownTimer(
//            context: Context,
//            user: User,
//            twoFactorAuth: String,
//            currentPassword: String,
//            delay: Long,
//            interval: Long
//        ) {
//            object : CountDownTimer(delay, interval) {
//                override fun onTick(millisUntilFinished: Long) {
//
//                    sweetAlertDialogMain.setOnShowListener {
//                        bindingTwoFactorAuth.tvResendCode.isEnabled = false
//
//                        val minutes = DateUtils.formatElapsedTime(millisUntilFinished.div(interval))
//                        if (minutes.contains(".")) minutes.replace(".", ":")
//
//                        bindingTwoFactorAuth.tvResendCode.text =
//                            "Code verification resend in ... $minutes"
//                        bindingTwoFactorAuth.tvResendCode.setTextColor(
//                            context.resources.getColor(
//                                R.color.white_70,
//                                context.theme
//                            )
//                        )
//                    }
//                }
//
//                override fun onFinish() {
//                    sweetAlertDialogMain.setOnShowListener {
//                        bindingTwoFactorAuth.tvResendCode.isEnabled = true
//                        bindingTwoFactorAuth.tvResendCode.text = "Resend verification code."
//                        bindingTwoFactorAuth.tvResendCode.setTextColor(
//                            context.resources.getColor(
//                                R.color.white,
//                                context.theme
//                            )
//                        )
//
//                        val currentCode = ManagerCallback.generateTokenCode()
//                        changeLayout(
//                            context,
//                            user,
//                            twoFactorAuth,
//                            currentPassword,
//                            currentCode,
//                            KEY_TWO_FACTOR_AUTH
//                        )
//
//                        bindingTwoFactorAuth.tvResendCode.setOnClickListener {
//                            val newCode = ManagerCallback.generateTokenCode()
//
//                            ManagerCallback.onStartSweetLoading(
//                                context,
//                                "Please wait",
//                                KEY_MESSAGE_TWO_FACTOR_AUTH
//                            )
//
//                            sendVerificationCode(
//                                context,
//                                user,
//                                twoFactorAuth,
//                                currentPassword,
//                                newCode,
//                            )
//                        }
//                    }
//                }
//            }
//        }
//
//
//        /**
//         * Check Email Exists from Database
//         */
//        private fun checkEmailExist(
//            user: User,
//            email: String,
//            currentPassword: String,
//            keyUpdate: Int
//        ) {
//            RestfulAPIService.requestMethod().checkEmailExist(email).enqueue(object :
//                Callback<RestfulAPIResponse> {
//                override fun onResponse(
//                    call: Call<RestfulAPIResponse>,
//                    response: Response<RestfulAPIResponse>
//                ) {
//                    if (response.isSuccessful) {
//                        when (response.body()!!.code) {
//                            200 -> {
//                                updateValue(
//                                    user.uid,
//                                    user.username,
//                                    email,
//                                    currentPassword,
//                                    0,
//                                    keyUpdate
//                                )
//                            }
//                            400 -> {
//                                val errorEmail = response.body()!!.errorValidation.email
//                                if (errorEmail.contains("valid email address"))
//                                    bindingEmail.tilEmail.error = errorEmail
//                                if (errorEmail.contains("unique value"))
//                                    bindingEmail.tilEmail.error = "Email is already exist."
//
//                                ManagerCallback.onStopSweetLoading()
//                            }
//                        }
//                    } else {
//                        ManagerCallback.onFailureSweetLoading(response.message())
//                    }
//
//                    Log.d("checkEmailExist", response.body().toString())
//                }
//
//                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
//                    ManagerCallback.onFailureSweetLoading(t.message!!)
//                    Log.d("checkEmailExist", t.message!!)
//                }
//            })
//        }
//
//
//        /**
//         * Update User
//         * - Username
//         * - Email
//         * - Password
//         * - Two Factor Authentication
//         */
//        private fun updateValue(
//            uid: Int,
//            username: String,
//            email: String,
//            password: String,
//            two_factor_auth: Int,
//            keyUpdate: Int
//        ) {
//
//            RestfulAPIService.requestMethod()
//                .updateUser(uid, username, email, password, two_factor_auth).enqueue(
//                    object : Callback<RestfulAPIResponse> {
//                        override fun onResponse(
//                            call: Call<RestfulAPIResponse>,
//                            response: Response<RestfulAPIResponse>
//                        ) {
//                            if (response.isSuccessful) {
//                                when (response.body()!!.code) {
//                                    200 -> {
//                                        when (keyUpdate) {
//                                            KEY_EMAIL -> ManagerCallback.onSuccessSweetLoading(
//                                                "Email successfully updated"
//                                            )
//                                            KEY_USERNAME -> ManagerCallback.onSuccessSweetLoading(
//                                                "Username successfully updated"
//                                            )
//                                            KEY_PASSWORD -> ManagerCallback.onSuccessSweetLoading(
//                                                "Password successfully updated"
//                                            )
//                                            KEY_TWO_FACTOR_AUTH -> {
//                                                if (two_factor_auth == 0)
//                                                    ManagerCallback.onSuccessSweetLoading(
//                                                        "Two Factor Authentication is disabled"
//                                                    )
//                                                else
//                                                    ManagerCallback.onSuccessSweetLoading(
//                                                        "Two Factor Authentication is enabled"
//                                                    )
//                                            }
//
//                                        }
//                                        EventBus.getDefault().postSticky(EventOnRefresh(true, ""))
//                                        sweetAlertDialogMain.dismissWithAnimation()
//                                    }
//                                    400 -> {
//                                        val errors = response.body()!!.errorValidation
//                                        when (keyUpdate) {
//                                            KEY_EMAIL -> bindingEmail.tilEmail.error = errors.email
//                                            KEY_USERNAME -> bindingUsername.tilUsername.error =
//                                                errors.username
//                                            KEY_PASSWORD -> bindingPassword.tilNewPassword.error =
//                                                errors.password
//                                        }
//                                        ManagerCallback.onStopSweetLoading()
//                                    }
//                                }
//
//                            } else {
//                                sweetAlertDialogMain.dismissWithAnimation()
//                                ManagerCallback.onFailureSweetLoading(response.message())
//                            }
//                            Log.d("updateUser", response.body().toString())
//                        }
//
//                        override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
//                            sweetAlertDialogMain.dismissWithAnimation()
//                            ManagerCallback.onFailureSweetLoading(t.message!!)
//                            Log.d("updateUser", t.message!!)
//                        }
//                    })
//        }
//
//
//        private fun onTextWatcherSweetAlertDialogUsername() {
//            bindingUsername.etUsername.addTextChangedListener(object : TextWatcher {
//                override fun beforeTextChanged(
//                    s: CharSequence?,
//                    start: Int,
//                    count: Int,
//                    after: Int
//                ) {
//                }
//
//                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                    if (s.isNotEmpty())
//                        bindingUsername.tilUsername.error = ""
//                }
//
//                override fun afterTextChanged(s: Editable?) {}
//            })
//
//            bindingUsername.etCurrentPassword.addTextChangedListener(object : TextWatcher {
//                override fun beforeTextChanged(
//                    s: CharSequence?,
//                    start: Int,
//                    count: Int,
//                    after: Int
//                ) {
//                }
//
//                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                    if (s.isNotEmpty())
//                        bindingUsername.tilCurrentPassword.error = ""
//                }
//
//                override fun afterTextChanged(s: Editable?) {}
//            })
//        }
//
//        private fun onTextWatcherSweetAlertDialogEmail() {
//            bindingEmail.etEmail.addTextChangedListener(object : TextWatcher {
//                override fun beforeTextChanged(
//                    s: CharSequence?,
//                    start: Int,
//                    count: Int,
//                    after: Int
//                ) {
//                }
//
//                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                    if (s.isNotEmpty())
//                        bindingEmail.tilEmail.error = ""
//                }
//
//                override fun afterTextChanged(s: Editable?) {}
//            })
//
//            bindingEmail.etCurrentPassword.addTextChangedListener(object : TextWatcher {
//                override fun beforeTextChanged(
//                    s: CharSequence?,
//                    start: Int,
//                    count: Int,
//                    after: Int
//                ) {
//                }
//
//                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                    if (s.isNotEmpty())
//                        bindingEmail.tilCurrentPassword.error = ""
//                }
//
//                override fun afterTextChanged(s: Editable?) {}
//            })
//        }
//
//        private fun onTextWatcherSweetAlertDialogPassword() {
//            bindingPassword.etNewPassword.addTextChangedListener(object : TextWatcher {
//                override fun beforeTextChanged(
//                    s: CharSequence?,
//                    start: Int,
//                    count: Int,
//                    after: Int
//                ) {
//                }
//
//                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                    if (s.isNotEmpty())
//                        bindingPassword.tilNewPassword.error = ""
//                }
//
//                override fun afterTextChanged(s: Editable?) {}
//            })
//
//            bindingPassword.etCurrentPassword.addTextChangedListener(object : TextWatcher {
//                override fun beforeTextChanged(
//                    s: CharSequence?,
//                    start: Int,
//                    count: Int,
//                    after: Int
//                ) {
//                }
//
//                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                    if (s.isNotEmpty())
//                        bindingPassword.tilCurrentPassword.error = ""
//                }
//
//                override fun afterTextChanged(s: Editable?) {}
//            })
//        }
//
//        private fun onTextWatcherSweetAlertDialogTwoFactorAuth() {
//            bindingTwoFactorAuth.etVerificationCode.addTextChangedListener(object : TextWatcher {
//                override fun beforeTextChanged(
//                    s: CharSequence?,
//                    start: Int,
//                    count: Int,
//                    after: Int
//                ) {
//                }
//
//                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                    if (s.isNotEmpty())
//                        bindingTwoFactorAuth.tilVerificationCode.error = ""
//                }
//
//                override fun afterTextChanged(s: Editable?) {}
//            })
//
//            bindingTwoFactorAuth.etCurrentPassword.addTextChangedListener(object : TextWatcher {
//                override fun beforeTextChanged(
//                    s: CharSequence?,
//                    start: Int,
//                    count: Int,
//                    after: Int
//                ) {
//                }
//
//                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                    if (s.isNotEmpty())
//                        bindingTwoFactorAuth.tilCurrentPassword.error = ""
//                }
//
//                override fun afterTextChanged(s: Editable?) {}
//            })
//        }


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
                ManagerCallback.onStartSweetLoading(context, "Please wait", "Delete account")

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
                        Log.d("deleteAllTodoList", response.body().toString())
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