package com.sabo.todolist_ci4_restful.Activity.Profile.Edit

import android.content.Context
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.sabo.todolist_ci4_restful.Activity.Profile.ProfileCallback
import com.sabo.todolist_ci4_restful.Helper.Callback.KeyStore
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
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
                        when (response.code()) {
                            200 -> {
                                val code = ManagerCallback.onGenerateTokenCode()
                                changeLayout(context, user, code)
                                ManagerCallback.sendVerificationCode(
                                    context,
                                    user,
                                    "Two Factor Authentication verification code",
                                    code
                                )
                                countDownTimer(context, user)
                            }
                            400 -> binding.tilCurrentPassword.error =
                                ManagerCallback.getErrorBody(response)!!.errorValidation.password
                            404 -> binding.tilCurrentPassword.error =
                                KeyStore.CURRENT_PASSWORD_WRONG
                            500 -> ManagerCallback.onSweetAlertDialogWarning(
                                context,
                                response.message()
                            )
                        }
                        binding.progressBar.visibility = View.GONE
                        ManagerCallback.onLog("reAuth_TwoFactorAuth", response)
                    }

                    override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                        binding.progressBar.visibility = View.GONE
                        ManagerCallback.onLog("reAuth_TwoFactorAuth", "${t.message}")
                        ManagerCallback.onSweetAlertDialogWarning(
                            context,
                            KeyStore.ON_FAILURE
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
                        sweetAlertDialog.findViewById<Button>(R.id.confirm_button)
                            .setBackgroundResource(R.drawable.red_button_background)
                    }
                    1 -> {
                        binding.tvSubtitle.text =
                            "Enter the verification code to enable two factor authentication."
                        sweetAlertDialog.confirmText = "Enable"
                        sweetAlertDialog.findViewById<Button>(R.id.confirm_button)
                            .setBackgroundResource(R.drawable.confirm_button_background)
                    }
                }

                sweetAlertDialog.setConfirmClickListener {
                    val inputCode = binding.etVerificationCode.text.toString()

                    if (inputCode != code)
                        binding.tilVerificationCode.error = KeyStore.VERIFICATION_CODE_WRONG

                    else {
                        countDownTimer.cancel()
                        ProfileCallback.onUpdateValues(
                            context,
                            sweetAlertDialog,
                            user,
                            KeyStore.KEY_TWO_FACTOR_AUTH
                        )
                    }
                }
                sweetAlertDialog.setCancelClickListener {
                    onClose()
                    countDownTimer.cancel()
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
                    binding.tvResendCode.text = KeyStore.RESEND_CODE
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
                            "Two Factor Authentication verification code",
                            newCode
                        )
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