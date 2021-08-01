package com.sabo.todolist_ci4_restful.Activity.Auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.sabo.todolist_ci4_restful.Activity.MainActivity
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Helper.SharedPreference.ManagerPreferences
import com.sabo.todolist_ci4_restful.R
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIResponse
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIService
import com.sabo.todolist_ci4_restful.databinding.ActivityLoginBinding
import com.sabo.todolist_ci4_restful.databinding.SweetAlertDialogTwoFactorAuthenticationBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sweetTwoFactorAuth: SweetAlertDialog
    private lateinit var bindingSweetTwoFactorAuth: SweetAlertDialogTwoFactorAuthenticationBinding
    private lateinit var countDownTimer: CountDownTimer

    private val DELAY: Long = 120000
    private val INTERVAL: Long = 1000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        onTextWatcher()
    }

    private fun initViews() {

        binding.tvSignUp.text = ManagerCallback.onGenerateTextViewButton(this, true)

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            clearErrorText()
            clearEditText()
        }

        binding.btnLogin.setOnClickListener {
            clearErrorText()
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            RestfulAPIService.requestMethod().signIn(email, password).enqueue(object :
                Callback<RestfulAPIResponse> {
                override fun onResponse(
                    call: Call<RestfulAPIResponse>,
                    response: Response<RestfulAPIResponse>
                ) {
                    if (response.isSuccessful) {
                        when (response.body()!!.code) {
                            200 -> {
                                clearEditText()
                                if (response.body()!!.user.two_factor_auth == 0)
                                    goToMainActivity(response)
                                else {
                                    val code = ManagerCallback.onGenerateTokenCode()
                                    showTwoFactorAuth(response, code)
                                    countDownTimer(response)
                                }

                            }
                            400 -> {
                                val validation = response.body()!!.errorValidation

                                if (!validation.email.isNullOrEmpty())
                                    binding.etEmail.setBackgroundResource(R.drawable.border_edit_text_error)
                                if (!validation.password.isNullOrEmpty())
                                    binding.etPassword.setBackgroundResource(R.drawable.border_edit_text_error)

                                binding.tilEmail.error = validation.email
                                binding.tilPassword.error = validation.password
                            }
                        }
                    } else {
                        if (response.message().contains("Not Found"))
                            binding.tilPassword.error =
                                "Your email or password was wrong. Or your account is not registered!"
                        else
                            binding.tilPassword.error = response.message()
                    }


                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    ManagerCallback.onLog("signIn", "$response", "${response.body()}")
                }

                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    ManagerCallback.onSweetAlertDialogWarning(
                        this@Login,
                        "Something wrong with server connection"
                    )
                    ManagerCallback.onLog("signIn", "${t.message}")
                }
            })
        }

        binding.tvForgotPassword.setOnClickListener {
            ForgotPassword.onStart(this)
        }
    }

    private fun goToMainActivity(response: Response<RestfulAPIResponse>) {
        ManagerPreferences.setIsLoggedIn(this@Login, true)
        ManagerPreferences.setUID(
            this@Login,
            response.body()!!.user.uid
        )

        Toast.makeText(
            this@Login,
            response.body()!!.message,
            Toast.LENGTH_SHORT
        ).show()

        startActivity(
            Intent(
                this@Login,
                MainActivity::class.java
            )
        )
        finish()
    }

    private fun showTwoFactorAuth(response: Response<RestfulAPIResponse>, code: String) {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.sweet_alert_dialog_two_factor_authentication, null)

        sweetTwoFactorAuth = SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
        bindingSweetTwoFactorAuth = SweetAlertDialogTwoFactorAuthenticationBinding.bind(view)

        sweetTwoFactorAuth.isShowCancelButton
        sweetTwoFactorAuth.cancelText = "Cancel"
        sweetTwoFactorAuth.confirmText = "Log In"
        sweetTwoFactorAuth.setOnShowListener {
            bindingSweetTwoFactorAuth.ibClose.setOnClickListener {
                sweetTwoFactorAuth.dismissWithAnimation()
            }

            bindingSweetTwoFactorAuth.etVerificationCode.addTextChangedListener(object :
                TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.isNotEmpty())
                        bindingSweetTwoFactorAuth.tilVerificationCode.error = ""
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
        sweetTwoFactorAuth.setCancelClickListener {
            sweetTwoFactorAuth.dismissWithAnimation()
            countDownTimer.cancel()
        }
        /** setConfirmListener */
        setConfirmListener(response, code)

        sweetTwoFactorAuth.show()
        ManagerCallback.initCustomSweetAlertDialog(this, view, sweetTwoFactorAuth)


        ManagerCallback.sendVerificationCode(
            this@Login,
            response.body()!!.user,
            "Log In verification code",
            code
        )
    }


    private fun setConfirmListener(
        response: Response<RestfulAPIResponse>,
        code: String
    ) {
        sweetTwoFactorAuth.setConfirmClickListener {
            val codeVerification = bindingSweetTwoFactorAuth.etVerificationCode.text.toString()
            if (codeVerification != code) {
                bindingSweetTwoFactorAuth.tilVerificationCode.error =
                    "Your verification code is wrong."
            } else {
                countDownTimer.cancel()
                sweetTwoFactorAuth.dismissWithAnimation()
                goToMainActivity(response)
            }
        }
    }

    private fun countDownTimer(response: Response<RestfulAPIResponse>) {
        countDownTimer = object : CountDownTimer(DELAY, INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                bindingSweetTwoFactorAuth.tvResendCode.isEnabled = false
                val minutes =
                    DateUtils.formatElapsedTime(millisUntilFinished.div(INTERVAL))
                        .replace(".", ":")

                bindingSweetTwoFactorAuth.tvResendCode.text =
                    "Code verification resend in ... $minutes"
                bindingSweetTwoFactorAuth.tvResendCode.setTextColor(
                    resources.getColor(
                        R.color.white_70,
                        theme
                    )
                )
            }

            override fun onFinish() {
                bindingSweetTwoFactorAuth.tvResendCode.isEnabled = true
                bindingSweetTwoFactorAuth.tvResendCode.text = "Resend verification code."
                bindingSweetTwoFactorAuth.tvResendCode.setTextColor(
                    resources.getColor(
                        R.color.white,
                        theme
                    )
                )

                val currentCode = ManagerCallback.onGenerateTokenCode()
                setConfirmListener(response, currentCode)

                bindingSweetTwoFactorAuth.tvResendCode.setOnClickListener {
                    val newCode = ManagerCallback.onGenerateTokenCode()
                    setConfirmListener(response, newCode)
                    ManagerCallback.sendVerificationCode(
                        this@Login,
                        response.body()!!.user,
                        "Log In verification code",
                        newCode
                    )
                    countDownTimer(response)
                }
            }

        }
        countDownTimer.start()
    }


    private fun clearEditText() {
        binding.etEmail.setText("")
        binding.etPassword.setText("")

        binding.etEmail.clearFocus()
        binding.etPassword.clearFocus()
    }

    private fun clearErrorText() {
        binding.tilEmail.error = ""
        binding.tilPassword.error = ""

        binding.etEmail.setBackgroundResource(R.drawable.border_edit_text_normal)
        binding.etPassword.setBackgroundResource(R.drawable.border_edit_text_normal)
    }

    private fun onTextWatcher() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    binding.tilEmail.error = ""
                    binding.etEmail.setBackgroundResource(R.drawable.border_edit_text_normal)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    binding.tilPassword.error = ""
                    binding.etPassword.setBackgroundResource(R.drawable.border_edit_text_normal)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

    }
}