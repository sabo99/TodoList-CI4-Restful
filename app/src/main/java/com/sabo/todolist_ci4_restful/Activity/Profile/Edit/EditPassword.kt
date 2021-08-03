package com.sabo.todolist_ci4_restful.Activity.Profile.Edit

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.sabo.todolist_ci4_restful.Activity.Profile.ProfileCallback
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Model.User
import com.sabo.todolist_ci4_restful.R
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIResponse
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIService
import com.sabo.todolist_ci4_restful.databinding.SweetAlertDialogEditPasswordBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditPassword {
    companion object {
        private lateinit var sweetAlertDialog: SweetAlertDialog
        private lateinit var binding: SweetAlertDialogEditPasswordBinding


        fun onUpdated(context: Context, user: User) {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.sweet_alert_dialog_edit_password, null)

            sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            binding = SweetAlertDialogEditPasswordBinding.bind(view)

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
                val newPassword = binding.etNewPassword.text.toString()

                if (newPassword == currentPassword && currentPassword.isNotEmpty())
                    binding.tilNewPassword.error =
                        "Your new password is still the same as the old password"
                else
                    reAuth(
                        context,
                        User(
                            user.uid,
                            user.username,
                            user.email,
                            newPassword,
                            "$currentPassword",
                            user.two_factor_auth
                        )
                    )

            }
            sweetAlertDialog.show()
            ManagerCallback.initCustomSweetAlertDialog(context, view, sweetAlertDialog)
        }

        private fun reAuth(context: Context, user: User) {
            binding.progressBar.visibility = View.VISIBLE

            val currentPassword = user.avatar!!
            RestfulAPIService.requestMethod().signIn(user.email, currentPassword).enqueue(object :
                Callback<RestfulAPIResponse> {
                override fun onResponse(
                    call: Call<RestfulAPIResponse>,
                    response: Response<RestfulAPIResponse>
                ) {
                    if (response.isSuccessful) {
                        when (response.body()!!.code) {
                            200 -> {
                                if (binding.etNewPassword.text.toString().isEmpty())
                                    binding.tilNewPassword.error = "The password is required."
                                else
                                    ProfileCallback.onUpdateValues(
                                        context,
                                        sweetAlertDialog, user, ProfileCallback.KEY_PASSWORD
                                    )
                            }
                            400 -> {
                                val errorPassword = response.body()!!.errorValidation.password

                                if (binding.etCurrentPassword.text.toString().isEmpty())
                                    binding.tilCurrentPassword.error = errorPassword
                                if (binding.etNewPassword.text.toString().isEmpty())
                                    binding.tilNewPassword.error = errorPassword
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
                    ManagerCallback.onLog("reAuth_Pass", "$response", "${response.body()}")
                }

                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    ManagerCallback.onSweetAlertDialogWarning(
                        context,
                        "Can't Change Password.\nSomething Wrong with server connection"
                    )
                    ManagerCallback.onLog("reAuth_Pass", "${t.message}")
                }
            })
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
                    if (s!!.isNotEmpty())
                        binding.tilCurrentPassword.error = ""
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

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s!!.isNotEmpty())
                        binding.tilNewPassword.error = ""
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        private fun onClose() {
            sweetAlertDialog.dismissWithAnimation()
        }
    }
}