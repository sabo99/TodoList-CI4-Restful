package com.sabo.todolist_ci4_restful.Activity.Profile.Edit

import android.app.Activity
import android.content.Context
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.sabo.todolist_ci4_restful.Activity.Profile.ProfileCallback
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Helper.JavaMailAPI.Credentials
import com.sabo.todolist_ci4_restful.Helper.JavaMailAPI.GMailSender
import com.sabo.todolist_ci4_restful.Model.User
import com.sabo.todolist_ci4_restful.R
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIResponse
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIService
import com.sabo.todolist_ci4_restful.databinding.SweetAlertDialogEnableTwoAuthBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TwoFactorAuth {
    companion object {
        private lateinit var sweetAlertDialog: SweetAlertDialog
        private lateinit var binding: SweetAlertDialogEnableTwoAuthBinding
        private lateinit var countDownTimer: CountDownTimer

        private const val DELAY: Long = 120000
        private const val INTERVAL: Long = 1000

        private const val LAYOUT_CURRENT_PASS = 1
        private const val LAYOUT_VERIFY_CODE = 2
        private var LAYOUT_KEY = LAYOUT_CURRENT_PASS


        fun onUpdated(context: Context, user: User) {
            LAYOUT_KEY = LAYOUT_CURRENT_PASS

            val view = LayoutInflater.from(context)
                .inflate(R.layout.sweet_alert_dialog_enable_two_auth, null)
            sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            binding = SweetAlertDialogEnableTwoAuthBinding.bind(view)

            sweetAlertDialog.setCancelable(false)
            sweetAlertDialog.isShowCancelButton
            sweetAlertDialog.cancelText = "Cancel"
            sweetAlertDialog.confirmText = "Check"
            sweetAlertDialog.setOnShowListener {
                when (LAYOUT_KEY) {
                    LAYOUT_CURRENT_PASS -> {
                        binding.layoutCurrentPassword.visibility = View.VISIBLE
                        binding.layoutVerificationCode.visibility = View.GONE
                    }
                    LAYOUT_VERIFY_CODE -> {
                        binding.layoutCurrentPassword.visibility = View.GONE
                        binding.layoutVerificationCode.visibility = View.VISIBLE
                    }
                }

                binding.ibClose.setOnClickListener { onClose() }

                onTextWatcher()
            }
            sweetAlertDialog.setCancelClickListener { onClose() }
            sweetAlertDialog.setConfirmClickListener {
                when (LAYOUT_KEY) {
                    LAYOUT_CURRENT_PASS -> {
                        val currentPassword = binding.etCurrentPassword.text.toString()
                        val twoFactorAuth = if (user.two_factor_auth == 0) 1 else 0

                        reAuth(
                            context,
                            User(
                                user.uid,
                                user.username,
                                user.email,
                                currentPassword,
                                "",
                                twoFactorAuth
                            )
                        )
                    }
                }

            }
            sweetAlertDialog.show()
            ManagerCallback.initCustomSweetAlertDialog(context, view, sweetAlertDialog)
        }

        private fun reAuth(context: Context, user: User) {
            binding.progressBar.visibility = View.VISIBLE

            RestfulAPIService.requestMethod().signIn(user.email, user.password!!)
                .enqueue(object : Callback<RestfulAPIResponse> {
                    override fun onResponse(
                        call: Call<RestfulAPIResponse>,
                        response: Response<RestfulAPIResponse>
                    ) {
                        if (response.isSuccessful) {
                            when (response.body()!!.code) {
                                200 -> {
                                    val code = ManagerCallback.generateTokenCode()
                                    changeLayout(context, user, code)
                                    sendVerificationCode(context, user, code)
                                    countDownTimer(context, user)
                                }
                                400 -> {
                                    val error = response.body()!!.errorValidation
                                    binding.tilCurrentPassword.error = error.password
                                }
                            }
                        } else {
                            if (response.message().contains("Not Found"))
                                binding.tilCurrentPassword.error = "Your current password was wrong."
                            else
                                ManagerCallback.onSweetAlertDialogWarning(
                                    context,
                                    response.message()
                                )
                        }

                        binding.progressBar.visibility = View.GONE
                        Log.d("reAuth-TwoFactorAuth", response.body().toString())
                    }

                    override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                        binding.progressBar.visibility = View.GONE
                        Log.d("reAuth-TwoFactorAuth", t.message!!)
                        ManagerCallback.onSweetAlertDialogWarning(
                            context,
                            "Something Wrong with server connection."
                        )
                    }
                })
        }

        private fun changeLayout(context: Context, user: User, code: String) {
            LAYOUT_KEY = LAYOUT_VERIFY_CODE

            if (LAYOUT_KEY == LAYOUT_VERIFY_CODE) {
                binding.layoutCurrentPassword.visibility = View.GONE
                binding.layoutVerificationCode.visibility = View.VISIBLE

                when (user.two_factor_auth) {
                    0 -> {
                        binding.tvSubtitle.text =
                            "Enter the verification code to disable two factor authentication."
                        sweetAlertDialog.confirmText = "Disable"
                        sweetAlertDialog.findViewById<Button>(R.id.confirm_button).setBackgroundResource(R.drawable.red_button_background)
                    }
                    1 -> {
                        binding.tvSubtitle.text =
                            "Enter the verification code to enable two factor authentication."
                        sweetAlertDialog.confirmText = "Enable"
                        sweetAlertDialog.findViewById<Button>(R.id.confirm_button).setBackgroundResource(R.drawable.confirm_button_background)
                    }
                }

                sweetAlertDialog.setConfirmClickListener {
                    val inputCode = binding.etVerificationCode.text.toString()

                    if (inputCode != code)
                        binding.tilVerificationCode.error = "Your verification code is wrong."
                    else {
                        countDownTimer.cancel()
                        ProfileCallback.onUpdateValues(
                            context,
                            sweetAlertDialog,
                            user,
                            ProfileCallback.KEY_TWO_FACTOR_AUTH
                        )
                    }
                }
                sweetAlertDialog.setCancelClickListener {
                    onClose()
                    countDownTimer.cancel()
                }
            }
        }

        private fun sendVerificationCode(context: Context, user: User, code: String) {
            ManagerCallback.onStartSweetLoading(context, "Code sent")

            Thread(Runnable {
                try {
                    val sender =
                        GMailSender(
                            Credentials.EMAIL_SENDER,
                            Credentials.PASSWORD_SENDER
                        )
                    sender.sendMail(
                        "Two Factor Authentication Verification Code",
                        "Code : $code",
                        "${Credentials.EMAIL_SENDER}",
                        "${user.email}"
                    )

                    (context as Activity).runOnUiThread {
                        ManagerCallback.onSuccessSweetLoading("Mail sent successfully")
                    }

                } catch (e: Exception) {
                    Log.d("SendEmail", e.message.toString())
                }
            }).start()
        }

        private fun countDownTimer(context: Context, user: User) {
            countDownTimer = object : CountDownTimer(DELAY, INTERVAL) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.tvResendCode.isEnabled = false
                    val minutes = DateUtils.formatElapsedTime(millisUntilFinished.div(INTERVAL))
                        .replace(".", ":")

                    binding.tvResendCode.text = "Code verification resend in ... $minutes"
                    binding.tvResendCode.setTextColor(
                        context.resources.getColor(
                            R.color.white_70,
                            context.theme
                        )
                    )
                }

                override fun onFinish() {
                    binding.tvResendCode.isEnabled = true
                    binding.tvResendCode.text = "Resend verification code."
                    binding.tvResendCode.setTextColor(
                        context.resources.getColor(
                            R.color.white,
                            context.theme
                        )
                    )

                    val currentCode = ManagerCallback.generateTokenCode()
                    changeLayout(context, user, currentCode)

                    binding.tvResendCode.setOnClickListener {
                        val newCode = ManagerCallback.generateTokenCode()
                        changeLayout(context, user, newCode)
                        sendVerificationCode(context, user, newCode)
                        countDownTimer(context, user)
                    }
                }
            }
            countDownTimer.start()
        }

        private fun onClose() {
            LAYOUT_KEY = LAYOUT_CURRENT_PASS
            sweetAlertDialog.dismissWithAnimation()
        }

        private fun onTextWatcher() {
            binding.etCurrentPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s!!.isNotEmpty()) binding.tilCurrentPassword.error = ""
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

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s!!.isNotEmpty()) binding.tilVerificationCode.error = ""
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }


    }
}