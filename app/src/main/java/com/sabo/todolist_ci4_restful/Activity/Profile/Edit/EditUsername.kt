package com.sabo.todolist_ci4_restful.Activity.Profile.Edit

import android.content.Context
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
import com.sabo.todolist_ci4_restful.databinding.SweetAlertDialogEditUsernameBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditUsername {
    companion object {
        private lateinit var sweetAlertDialog: SweetAlertDialog
        private lateinit var binding: SweetAlertDialogEditUsernameBinding

        fun onUpdated(context: Context, user: User) {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.sweet_alert_dialog_edit_username, null)

            sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            binding = SweetAlertDialogEditUsernameBinding.bind(view)

            sweetAlertDialog.setCancelable(false)
            sweetAlertDialog.isShowCancelButton
            sweetAlertDialog.cancelText = "Cancel"
            sweetAlertDialog.confirmText = "Done"
            sweetAlertDialog.setOnShowListener {
                binding.ibClose.setOnClickListener { onClose() }

                binding.tilUsername.suffixText = ManagerCallback.onHashNumber(user.uid)
                binding.etUsername.setText(user.username)
                onTextWatcher()
            }
            sweetAlertDialog.setCancelClickListener { onClose() }
            sweetAlertDialog.setConfirmClickListener {
                val currentPassword = binding.etCurrentPassword.text.toString()
                val newUsername = binding.etUsername.text.toString()

                if (newUsername == user.username) onClose()
                else
                    reAuth(
                        context,
                        User(
                            user.uid,
                            newUsername,
                            user.email,
                            currentPassword,
                            "",
                            user.two_factor_auth
                        )
                    )
            }
            sweetAlertDialog.show()
            ManagerCallback.initCustomSweetAlertDialog(context, view, sweetAlertDialog)
        }

        private fun reAuth(context: Context, user: User) {
            binding.progressBar.visibility = View.VISIBLE

            RestfulAPIService.requestMethod().signIn(user.email, user.password!!).enqueue(object :
                Callback<RestfulAPIResponse> {
                override fun onResponse(
                    call: Call<RestfulAPIResponse>,
                    response: Response<RestfulAPIResponse>
                ) {
                    when (response.body()!!.code) {
                        200 -> ProfileCallback.onUpdateValues(
                            context,
                            sweetAlertDialog,
                            user,
                            KeyStore.KEY_USERNAME
                        )
                        400 -> binding.tilCurrentPassword.error =
                            ManagerCallback.getErrorBody(response)!!.errorValidation.password
                        404 -> binding.tilCurrentPassword.error = KeyStore.CURRENT_PASSWORD_WRONG
                        500 -> ManagerCallback.onSweetAlertDialogWarning(
                            context,
                            response.message()
                        )
                    }
                    binding.progressBar.visibility = View.GONE
                    ManagerCallback.onLog("reAuth_Username", response)
                }

                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    ManagerCallback.onSweetAlertDialogWarning(
                        context,
                        "Can't Change Username.\n${KeyStore.ON_FAILURE}"
                    )
                    ManagerCallback.onLog("reAuth_Username", "${t.message}")
                }

            })
        }

        private fun onTextWatcher() {
            binding.etUsername.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s!!.isNotEmpty())
                        binding.tilUsername.error = ""
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