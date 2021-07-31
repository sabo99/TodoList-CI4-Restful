package com.sabo.todolist_ci4_restful.Activity.Auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sabo.todolist_ci4_restful.Activity.MainActivity
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Helper.SharedPreference.ManagerPreferences
import com.sabo.todolist_ci4_restful.R
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIResponse
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIService
import com.sabo.todolist_ci4_restful.databinding.ActivitySignUpBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        onTextWatcher()
    }

    private fun initViews() {

        binding.tvLogin.text = ManagerCallback.generateTextViewButton(this, false)

        binding.tvLogin.setOnClickListener {
            finish()
            clearErrorText()
            clearEditText()
        }

        binding.btnSignUp.setOnClickListener {
            clearErrorText()
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSignUp.isEnabled = false

            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val passwordConfirm = binding.etPasswordConfirm.text.toString()

            RestfulAPIService.requestMethod().signUp(username, email, password, passwordConfirm)
                .enqueue(
                    object : Callback<RestfulAPIResponse> {
                        override fun onResponse(
                            call: Call<RestfulAPIResponse>,
                            response: Response<RestfulAPIResponse>
                        ) {
                            when (response.body()!!.code) {
                                201 -> {
                                    clearEditText()

                                    ManagerPreferences.setIsLoggedIn(this@SignUp, true)
                                    ManagerPreferences.setUID(
                                        this@SignUp,
                                        response.body()!!.user.uid
                                    )

                                    Toast.makeText(
                                        this@SignUp,
                                        response.message(),
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    startActivity(
                                        Intent(
                                            this@SignUp,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                                400 -> {
                                    val validation = response.body()!!.errorValidation

                                    if (!validation.username.isNullOrEmpty())
                                        binding.etUsername.setBackgroundResource(R.drawable.border_edit_text_error)
                                    if (!validation.email.isNullOrEmpty())
                                        binding.etEmail.setBackgroundResource(R.drawable.border_edit_text_error)
                                    if (!validation.password.isNullOrEmpty())
                                        binding.etPassword.setBackgroundResource(R.drawable.border_edit_text_error)
                                    if (!validation.password_confirm.isNullOrEmpty())
                                        binding.etPasswordConfirm.setBackgroundResource(R.drawable.border_edit_text_error)

                                    binding.tilUsername.error = validation.username
                                    binding.tilEmail.error = validation.email
                                    binding.tilPassword.error = validation.password
                                    binding.tilPasswordConfirm.error = validation.password_confirm

                                }
                                else -> {
                                    Toast.makeText(
                                        this@SignUp,
                                        response.message(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            binding.progressBar.visibility = View.GONE
                            binding.btnSignUp.isEnabled = true
                        }

                        override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                            Log.d("reponse", t.message!!)
                            Toast.makeText(
                                this@SignUp,
                                t.message!!,
                                Toast.LENGTH_SHORT
                            ).show()

                            binding.progressBar.visibility = View.GONE
                            binding.btnSignUp.isEnabled = true
                        }
                    })
        }
    }

    private fun clearEditText() {
        binding.etUsername.setText("")
        binding.etEmail.setText("")
        binding.etPassword.setText("")
        binding.etPasswordConfirm.setText("")

        binding.etUsername.clearFocus()
        binding.etEmail.clearFocus()
        binding.etPassword.clearFocus()
        binding.etPasswordConfirm.clearFocus()
    }

    private fun clearErrorText() {
        binding.tilUsername.error = ""
        binding.tilEmail.error = ""
        binding.tilPassword.error = ""
        binding.tilPasswordConfirm.error = ""
    }

    private fun onTextWatcher() {
        binding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()){
                    binding.tilUsername.error = ""
                    binding.etUsername.setBackgroundResource(R.drawable.border_edit_text_normal)
                }

            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()){
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
                if (s.toString().isNotEmpty()){
                    binding.tilPassword.error = ""
                    binding.etPassword.setBackgroundResource(R.drawable.border_edit_text_normal)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.etPasswordConfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()){
                    binding.tilPasswordConfirm.error = ""
                    binding.etPasswordConfirm.setBackgroundResource(R.drawable.border_edit_text_normal)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }
}