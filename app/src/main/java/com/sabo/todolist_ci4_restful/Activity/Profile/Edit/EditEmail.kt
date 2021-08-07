package com.sabo.todolist_ci4_restful.Activity.Profile.Edit

import android.content.Context
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.sabo.todolist_ci4_restful.Activity.Profile.ProfileCallback
import com.sabo.todolist_ci4_restful.Helper.Callback.KeyStore
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Model.User
import com.sabo.todolist_ci4_restful.R
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIResponse
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIService
import com.sabo.todolist_ci4_restful.databinding.SweetAlertDialogEditEmailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditEmail {
    companion object {
        private lateinit var sweetAlertDialog: SweetAlertDialog
        private lateinit var binding: SweetAlertDialogEditEmailBinding
        private lateinit var countDownTimer: CountDownTimer

        private const val LAYOUT_EMAIL = 1
        private const val LAYOUT_VERIFY_CODE = 2
        private var LAYOUT_KEY = LAYOUT_EMAIL


        fun onUpdated(context: Context, user: User) {
            LAYOUT_KEY = LAYOUT_EMAIL

            val view = LayoutInflater.from(context)
                .inflate(R.layout.sweet_alert_dialog_edit_email, null)

            sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            binding = SweetAlertDialogEditEmailBinding.bind(view)

            sweetAlertDialog.setCancelable(false)
            sweetAlertDialog.isShowCancelButton
            sweetAlertDialog.cancelText = "Cancel"
            sweetAlertDialog.confirmText = "Done"
            sweetAlertDialog.setOnShowListener {
                when (LAYOUT_KEY) {
                    LAYOUT_EMAIL -> {
                        binding.layoutEmail.visibility = View.VISIBLE
                        binding.layoutVerificationCode.visibility = View.GONE
                    }
                    LAYOUT_VERIFY_CODE -> {
                        binding.layoutEmail.visibility = View.GONE
                        binding.layoutVerificationCode.visibility = View.VISIBLE
                    }
                }

                binding.ibClose.setOnClickListener { onClose() }

                onTextWatcher()
            }
            sweetAlertDialog.setCancelClickListener { onClose() }
            sweetAlertDialog.setConfirmClickListener {
                when (LAYOUT_KEY) {
                    LAYOUT_EMAIL -> {
                        val currentPassword = binding.etCurrentPassword.text.toString()
                        val newEmail = binding.etEmail.text.toString()

                        if (newEmail == user.email) binding.tilEmail.error =
                            "Email has been used on your account."
                        else
                            reAuth(
                                context,
                                User(
                                    user.uid,
                                    user.username,
                                    newEmail,
                                    currentPassword,
                                    "${user.email}",
                                    user.two_factor_auth
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

            val currentEmail = user.avatar!!
            RestfulAPIService.requestMethod().signIn(currentEmail, user.password!!).enqueue(object :
                Callback<RestfulAPIResponse> {
                override fun onResponse(
                    call: Call<RestfulAPIResponse>,
                    response: Response<RestfulAPIResponse>
                ) {
                    when (response.code()) {
                        200 -> checkEmailExist(context, user)
                        400 -> binding.tilCurrentPassword.error =
                            ManagerCallback.getErrorBody(response)!!.errorValidation.password
                        404 -> binding.tilCurrentPassword.error = KeyStore.CURRENT_PASSWORD_WRONG
                        500 -> ManagerCallback.onSweetAlertDialogWarning(
                            context,
                            response.message()
                        )
                    }

                    binding.progressBar.visibility = View.GONE
                    ManagerCallback.onLog("reAuth_Email", response)
                }

                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    ManagerCallback.onSweetAlertDialogWarning(
                        context,
                        "Can't Change Email.\n${KeyStore.ON_FAILURE}"
                    )
                    ManagerCallback.onLog("reAuth_Email", "${t.message}")
                }
            })
        }

        private fun checkEmailExist(context: Context, user: User) {
            binding.progressBar.visibility = View.VISIBLE
            RestfulAPIService.requestMethod().checkEmailExist(user.email).enqueue(object :
                Callback<RestfulAPIResponse> {
                override fun onResponse(
                    call: Call<RestfulAPIResponse>,
                    response: Response<RestfulAPIResponse>
                ) {
                    when (response.code()) {
                        200 -> {
                            val code = ManagerCallback.onGenerateTokenCode()
                            changeLayout(context, user, code)
                            ManagerCallback.sendVerificationCode(
                                context,
                                user,
                                "Verify Change Email code",
                                code
                            )
                            countDownTimer(context, user)
                        }
                        400 -> binding.tilEmail.error =
                            ManagerCallback.getErrorBody(response)!!.errorValidation.email
                        500 -> ManagerCallback.onSweetAlertDialogWarning(
                            context,
                            response.message()
                        )
                    }

                    binding.progressBar.visibility = View.GONE
                    ManagerCallback.onLog("checkEmailExists", response)
                }

                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    ManagerCallback.onSweetAlertDialogWarning(
                        context,
                        "Can't CheckEmailExist.\n${KeyStore.ON_FAILURE}"
                    )
                    ManagerCallback.onLog("checkEmailExists", "${t.message}")
                }
            })
        }

        private fun changeLayout(context: Context, user: User, code: String) {
            LAYOUT_KEY = LAYOUT_VERIFY_CODE
            if (LAYOUT_KEY == LAYOUT_VERIFY_CODE) {
                binding.layoutEmail.visibility = View.GONE
                binding.layoutVerificationCode.visibility = View.VISIBLE

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
                            KeyStore.KEY_EMAIL
                        )
                    }
                }
                sweetAlertDialog.setCancelClickListener {
                    countDownTimer.cancel()
                    onClose()
                }
            }
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
                    binding.tvResendCode.text = "Resend verification code."
                    binding.tvResendCode.setTextColor(
                        context.resources.getColor(
                            R.color.white,
                            context.theme
                        )
                    )

                    val currentCode = ManagerCallback.onGenerateTokenCode()
                    changeLayout(context, user, currentCode)

                    binding.tvResendCode.setOnClickListener {
                        val newCode = ManagerCallback.onGenerateTokenCode()
                        changeLayout(context, user, newCode)
                        ManagerCallback.sendVerificationCode(
                            context,
                            user,
                            "Verify Change Email code",
                            newCode
                        )
                        countDownTimer(context, user)
                    }
                }
            }

            countDownTimer.start()
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

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s!!.isNotEmpty())
                        binding.tilEmail.error = ""
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            binding.etCurrentPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s!!.isNotEmpty())
                        binding.tilCurrentPassword.error = ""
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        private fun onClose() {
            sweetAlertDialog.dismissWithAnimation()
        }

    }
}
