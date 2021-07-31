package com.sabo.todolist_ci4_restful.Activity.Profile.Edit

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.sabo.todolist_ci4_restful.Activity.Profile.ProfileCallback
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

        fun onUpdated(context: Context, user: User) {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.sweet_alert_dialog_edit_email, null)

            sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            binding = SweetAlertDialogEditEmailBinding.bind(view)

            sweetAlertDialog.setCancelable(false)
            sweetAlertDialog.isShowCancelButton
            sweetAlertDialog.cancelText = "Cancel"
            sweetAlertDialog.confirmText = "Done"
            sweetAlertDialog.setOnShowListener {
                binding.ibClose.setOnClickListener { onClose() }

                onTextWatcher()
            }
            sweetAlertDialog.setCancelClickListener { onClose() }
            sweetAlertDialog.setConfirmClickListener {
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
                    if (response.isSuccessful) {
                        when (response.body()!!.code) {
                            200 -> checkEmailExist(context, user)
                            400 -> {
                                binding.progressBar.visibility = View.GONE
                                val errors = response.body()!!.errorValidation
                                binding.tilCurrentPassword.error = errors.password
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
                        binding.progressBar.visibility = View.GONE
                    }

                    Log.d("reAuth-EditEmail", response.body().toString())
                }

                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Log.d("reAuth-EditEmail", t.message!!)
                    ManagerCallback.onSweetAlertDialogWarning(
                        context,
                        "Can't Change Email.\nSomething Wrong with server connection."
                    )
                }
            })
        }

        private fun checkEmailExist(context: Context, user: User) {
            RestfulAPIService.requestMethod().checkEmailExist(user.email).enqueue(object :
                Callback<RestfulAPIResponse> {
                override fun onResponse(
                    call: Call<RestfulAPIResponse>,
                    response: Response<RestfulAPIResponse>
                ) {
                    if (response.isSuccessful) {
                        when (response.body()!!.code) {
                            200 -> {
                                ProfileCallback.onUpdateValues(context, sweetAlertDialog, user, ProfileCallback.KEY_EMAIL)
                            }
                            400 -> {
                                val errors = response.body()!!.errorValidation
                                if (errors.email.contains("valid email address"))
                                    binding.tilEmail.error = errors.email
                                if (errors.email.contains("unique value"))
                                    binding.tilEmail.error = "Email is already exist."
                            }
                        }
                    } else
                        ManagerCallback.onSweetAlertDialogWarning(context, response.message())

                    Log.d("checkEmail-EditEmail", response.body().toString())

                    binding.progressBar.visibility = View.GONE
                }

                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Log.d("checkEmail-EditEmail", t.message!!)
                    ManagerCallback.onSweetAlertDialogWarning(context, t.message!!)
                }
            })
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