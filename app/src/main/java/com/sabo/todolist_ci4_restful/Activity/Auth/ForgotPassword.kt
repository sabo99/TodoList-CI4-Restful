package com.sabo.todolist_ci4_restful.Activity.Auth

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.sabo.todolist_ci4_restful.Helper.Callback.KeyStore
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Model.User
import com.sabo.todolist_ci4_restful.R
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIResponse
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIService
import com.sabo.todolist_ci4_restful.databinding.SweetAlertDialogForgotPasswordBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ForgotPassword {
    companion object {
        private lateinit var sweetAlertDialog: SweetAlertDialog
        private lateinit var binding: SweetAlertDialogForgotPasswordBinding
        private lateinit var countDownTimer: CountDownTimer

        /** 1000 = 1 seconds */
        private const val LAYOUT_EMAIL = 103103
        private const val LAYOUT_PASSWORD = 104104
        private var LAYOUT_KEY = LAYOUT_EMAIL


        fun onStart(context: Context) {

            LAYOUT_KEY = LAYOUT_EMAIL

            val view = LayoutInflater.from(context)
                .inflate(R.layout.sweet_alert_dialog_forgot_password, null)
            sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            binding = SweetAlertDialogForgotPasswordBinding.bind(view)

            sweetAlertDialog.setCancelable(false)
            sweetAlertDialog.isShowCancelButton
            sweetAlertDialog.cancelText = "Cancel"
            sweetAlertDialog.confirmText = "Confirm"
            sweetAlertDialog.setOnShowListener {
                if (LAYOUT_KEY == LAYOUT_EMAIL) {
                    binding.layoutEmail.visibility = View.VISIBLE
                    binding.layoutPassword.visibility = View.GONE
                }
                if (LAYOUT_KEY == LAYOUT_PASSWORD) {
                    binding.layoutEmail.visibility = View.GONE
                    binding.layoutPassword.visibility = View.VISIBLE
                }

                binding.ibClose.setOnClickListener { onClose() }

                onTextWatcher()
            }
            sweetAlertDialog.setCancelClickListener { onClose() }
            sweetAlertDialog.setConfirmClickListener {

                if (LAYOUT_KEY == LAYOUT_EMAIL) checkEmail(context)
            }

            sweetAlertDialog.show()
            ManagerCallback.initCustomSweetAlertDialog(context, view, sweetAlertDialog)
        }

        private fun onClose() {
            LAYOUT_KEY = LAYOUT_EMAIL
            sweetAlertDialog.dismissWithAnimation()
        }

        private fun onTextWatcher() {
            binding.etEmail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.toString().isNotEmpty())
                        binding.tilEmail.error = ""
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            binding.etNewPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.toString().isNotEmpty())
                        binding.tilNewPassword.error = ""
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            binding.etVerificationCode.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.toString().isNotEmpty())
                        binding.tilVerificationCode.error = ""
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        private fun checkEmail(context: Context) {
            val email = binding.etEmail.text.toString()

            if (email.isEmpty()) {
                binding.tilEmail.error = "The email field is required."
                return
            }

            binding.progressBar.visibility = View.VISIBLE

            RestfulAPIService.requestMethod().forgotPassword(email).enqueue(object :
                Callback<RestfulAPIResponse> {
                override fun onResponse(
                    call: Call<RestfulAPIResponse>,
                    response: Response<RestfulAPIResponse>
                ) {
                    when (response.code()) {
                        200 -> {
                            val user = response.body()!!.user
                            val code = ManagerCallback.onGenerateTokenCode()
                            changeLayoutPassword(context, user, code)
                            ManagerCallback.sendVerificationCode(
                                context,
                                user,
                                "Email verification code",
                                code
                            )
                            countDownTimer(context, user)
                        }
                        400 -> binding.tilEmail.error =
                                    ManagerCallback.getErrorBody(response)!!.errorValidation.email
                        404 -> binding.tilEmail.error = "Your email is not registered."
                        500 -> ManagerCallback.onSweetAlertDialogWarning(
                            context,
                            response.message()
                        )
                    }

                    binding.progressBar.visibility = View.GONE
                    ManagerCallback.onLog("forgotPassword", response)
                }

                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    ManagerCallback.onLog("forgotPassword", "${t.message}")
                }
            })
        }


        private fun changeLayoutPassword(context: Context, user: User, code: String) {
            LAYOUT_KEY = LAYOUT_PASSWORD

            if (LAYOUT_KEY == LAYOUT_PASSWORD) {
                sweetAlertDialog.confirmText = "Done"
                binding.layoutEmail.visibility = View.GONE
                binding.layoutPassword.visibility = View.VISIBLE

                sweetAlertDialog.setConfirmClickListener {
                    val inputCode = binding.etVerificationCode.text.toString()
                    val newPassword = binding.etNewPassword.text.toString()

                    if (newPassword.isEmpty()) {
                        binding.tilNewPassword.error = "The password is required."
                    } else {
                        if (inputCode != code)
                            binding.tilVerificationCode.error = KeyStore.VERIFICATION_CODE_WRONG
                        else {
                            countDownTimer.cancel()
                            binding.progressBar.visibility = View.VISIBLE
                            Handler().postDelayed({
                                updateNewPassword(context, user, newPassword)
                            }, 2000)
                        }
                    }
                }
                sweetAlertDialog.setCancelClickListener {
                    onClose()
                    countDownTimer.cancel()
                }
            }
        }

        private fun updateNewPassword(context: Context, user: User, newPassword: String) {
            RestfulAPIService.requestMethod()
                .updateUser(user.uid, user.username, user.email, newPassword, user.two_factor_auth)
                .enqueue(
                    object : Callback<RestfulAPIResponse> {
                        override fun onResponse(
                            call: Call<RestfulAPIResponse>,
                            response: Response<RestfulAPIResponse>
                        ) {
                            when (response.code()) {
                                200 -> {
                                    sweetAlertDialog.dismissWithAnimation()
                                    Toast.makeText(
                                        context,
                                        "Password has been successfully change.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                400 -> ManagerCallback.onLog("updatePassword", ManagerCallback.getErrorBody(response)!!.toString())
                                500 -> ManagerCallback.onSweetAlertDialogWarning(context, response.message())
                            }
                            binding.progressBar.visibility = View.GONE
                            ManagerCallback.onCreateLogUser(user.uid, KeyStore.UPDATE_PASSWORD)
                            ManagerCallback.onLog("updatePassword", response)
                        }

                        override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                            binding.progressBar.visibility = View.GONE
                            ManagerCallback.onSweetAlertDialogWarning(context, KeyStore.ON_FAILURE)
                            ManagerCallback.onLog("updatePassword", "${t.message}")
                        }
                    })
        }

        private fun countDownTimer(context: Context, user: User) {
            countDownTimer = object : CountDownTimer(KeyStore.DELAY, KeyStore.INTERVAL) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.tvResendCode.isEnabled = false
                    binding.tvResendCode.text = ManagerCallback.elapsedTimeVerificationCode(
                        millisUntilFinished.div(
                            KeyStore.INTERVAL
                        )
                    )
                    binding.tvResendCode.setTextColor(
                        context.resources.getColor(
                            R.color.white_70,
                            context.theme
                        )
                    )
                }

                override fun onFinish() {
                    binding.tvResendCode.isEnabled = true
                    binding.tvResendCode.text = KeyStore.RESEND_CODE
                    binding.tvResendCode.setTextColor(
                        context.resources.getColor(
                            R.color.white,
                            context.theme
                        )
                    )

                    val currentCode = ManagerCallback.onGenerateTokenCode()
                    changeLayoutPassword(context, user, currentCode)

                    binding.tvResendCode.setOnClickListener {
                        val newCode = ManagerCallback.onGenerateTokenCode()
                        changeLayoutPassword(context, user, newCode)
                        ManagerCallback.sendVerificationCode(
                            context,
                            user,
                            "Email verification code",
                            newCode
                        )
                        countDownTimer(context, user)
                    }
                }
            }
            countDownTimer.start()
        }
    }
}