package com.sabo.todolist_ci4_restful.Helper.Callback

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.Html.fromHtml
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.sabo.todolist_ci4_restful.Helper.JavaMailAPI.Credentials
import com.sabo.todolist_ci4_restful.Helper.JavaMailAPI.GMailSender
import com.sabo.todolist_ci4_restful.Model.User
import com.sabo.todolist_ci4_restful.R
import java.util.*
import kotlin.math.abs

class ManagerCallback {
    companion object {

        private lateinit var sweetLoading: SweetAlertDialog
        private lateinit var linearLayout: LinearLayout
        private lateinit var titleText: TextView
        private lateinit var contentText: TextView
        private lateinit var confirmButton: Button
        private lateinit var cancelButton: Button

        const val MULTIPLE_PERMISSION = 333

        fun onStartSweetLoading(context: Context, content: String) {
            sweetLoading = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
            sweetLoading.setCancelable(false)
            sweetLoading.progressHelper.barColor =
                context.resources.getColor(R.color.blue, context.theme)
            sweetLoading.titleText = "Please wait..."
            if (content.isNotEmpty()) {
                sweetLoading.showContentText(true)
                sweetLoading.contentText = "$content."
            } else
                sweetLoading.showContentText(false)

            sweetLoading.show()

            initCustomSweetAlertDialog(context, null, sweetLoading)

        }

        fun onStopSweetLoading() {
            sweetLoading.dismissWithAnimation()
            sweetLoading.dismiss()
        }

        fun onSuccessSweetLoading(content: String) {
            sweetLoading.setCustomImage(R.drawable.ic_round_check_circle_outline_128)
            sweetLoading.titleText = "Success"
            sweetLoading.contentText = "$content."
            sweetLoading.setConfirmClickListener { onStopSweetLoading() }
            sweetLoading.changeAlertType(SweetAlertDialog.CUSTOM_IMAGE_TYPE)

            sweetLoading.findViewById<Button>(R.id.confirm_button)
                .setBackgroundResource(R.drawable.confirm_button_background)
        }

        fun onFailureSweetLoading(message: String) {
            sweetLoading.titleText = "Oops!"
            sweetLoading.contentText = message
            sweetLoading.setConfirmClickListener { onStopSweetLoading() }
            sweetLoading.changeAlertType(SweetAlertDialog.WARNING_TYPE)
        }

        fun onSweetAlertDialogWarning(
            context: Context,
            content: String
        ) {
            val sweet = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
            sweet.setCancelable(false)
            sweet.titleText = "Oops!"
            sweet.contentText = "$content."
            sweet.show()
            initCustomSweetAlertDialog(context, null, sweet)
        }

        fun initCustomSweetAlertDialog(
            context: Context,
            view: View?,
            sweetAlertDialog: SweetAlertDialog
        ) {
            linearLayout = sweetAlertDialog.findViewById(R.id.loading)
            titleText = sweetAlertDialog.findViewById(R.id.title_text)
            contentText = sweetAlertDialog.findViewById(R.id.content_text)
            confirmButton = sweetAlertDialog.findViewById(R.id.confirm_button)
            cancelButton = sweetAlertDialog.findViewById(R.id.cancel_button)

            if (view != null) {
                val index = linearLayout.indexOfChild(linearLayout.findViewById(R.id.content_text))
                linearLayout.addView(view, index + 1)
                titleText.visibility = View.GONE
                confirmButton.setBackgroundResource(R.drawable.confirm_button_background)
            } else
                titleText.visibility = View.VISIBLE

            linearLayout.setBackgroundResource(R.drawable.sweet_alert_dialog_background)
            titleText.setTextColor(context.resources.getColor(R.color.white, context.theme))
            contentText.setTextColor(context.resources.getColor(R.color.white_70, context.theme))
            cancelButton.setBackgroundResource(R.drawable.cancel_button_background)
        }


        fun onTagNumber(integer: Int): String {
            return when (integer.toString().length) {
                1 -> "#000$integer"
                2 -> "#00$integer"
                3 -> "#0$integer"
                else -> "#$integer"
            }
        }

        fun onGenerateTokenCode(): String {
            val value = StringBuilder()
                .append(System.currentTimeMillis())
                /** Get current time in millisecond  */
                .append(abs(Random().nextInt()))
                /** Add random number to block same order at same time  */
                .toString()

            val x1 = value.substring(0, 6).reversed().toInt()
            val x2 = value.substring(7, 13).toInt()
            val x3 = value.substring(14, 16).reversed().toInt()
            val result = x2 + x3 + x1
            return "$result".reversed()
        }

        fun onGenerateTextViewButton(context: Context, isLogin: Boolean): CharSequence? {

            val login = "<font color='${
                context.resources.getColor(R.color.white_45, context.theme)
            }'>Don't have an account? </font><font color='#ffffff'><b>Sign Up</b></font>"

            val signUp = "<font color='${
                context.resources.getColor(R.color.white_45, context.theme)
            }'>Already have an account? </font><font color='#ffffff'><b>Log In</b></font>"

            return when (isLogin) {
                true -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) fromHtml(login, 0)
                    else "Don't have an account? Sign Up"
                }
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) fromHtml(signUp, 0)
                    else "Already have an account? Log In"
                }
            }
        }

        fun sendVerificationCode(context: Context, user: User, subject: String, code: String){
            onStartSweetLoading(context, "Code sent")

            Thread(Runnable {
                try {
                    val sender =
                        GMailSender(
                            Credentials.EMAIL_SENDER,
                            Credentials.PASSWORD_SENDER
                        )
                    sender.sendMail(
                        "$subject : $code",
                        "This code will expire in 2 minutes.",
                        "${Credentials.EMAIL_SENDER}",
                        "${user.email}"
                    )

                    (context as Activity).runOnUiThread {
                        onSuccessSweetLoading("Mail sent successfully")
                    }

                } catch (e: Exception) {
                    onLog("SendEmail", "${e.message}")
                }
            }).start()
        }

        fun onLog(tag: String, response: String, body: String) {
            Log.d(tag, response)
            Log.d(tag, body)
        }

        fun onLog(tag: String, message: String) {
            Log.d(tag, message)
        }

    }
}