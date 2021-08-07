package com.sabo.todolist_ci4_restful.Helper.Callback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html.fromHtml
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.sabo.todolist_ci4_restful.Activity.Auth.Login
import com.sabo.todolist_ci4_restful.Helper.JavaMailAPI.Credentials
import com.sabo.todolist_ci4_restful.Helper.JavaMailAPI.GMailSender
import com.sabo.todolist_ci4_restful.Helper.SharedPreference.ManagerPreferences
import com.sabo.todolist_ci4_restful.Model.User
import com.sabo.todolist_ci4_restful.R
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIResponse
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.NetworkInterface
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
        private lateinit var timer: Timer

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

            initCustomSweetAlertDialog(context, sweetLoading)
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
            initCustomSweetAlertDialog(context, sweet)
        }

        fun initCustomSweetAlertDialog(
            context: Context,
            view: View,
            sweetAlertDialog: SweetAlertDialog
        ) {
            linearLayout = sweetAlertDialog.findViewById(R.id.loading)
            titleText = sweetAlertDialog.findViewById(R.id.title_text)
            contentText = sweetAlertDialog.findViewById(R.id.content_text)
            confirmButton = sweetAlertDialog.findViewById(R.id.confirm_button)
            cancelButton = sweetAlertDialog.findViewById(R.id.cancel_button)

            val index = linearLayout.indexOfChild(linearLayout.findViewById(R.id.content_text))
            linearLayout.addView(view, index + 1)
            titleText.visibility = View.GONE
            confirmButton.setBackgroundResource(R.drawable.confirm_button_background)

            linearLayout.setBackgroundResource(R.drawable.sweet_alert_dialog_background)
            titleText.setTextColor(context.resources.getColor(R.color.white, context.theme))
            contentText.setTextColor(context.resources.getColor(R.color.white_70, context.theme))
            cancelButton.setBackgroundResource(R.drawable.cancel_button_background)
        }

        fun initCustomSweetAlertDialog(
            context: Context,
            sweetAlertDialog: SweetAlertDialog
        ) {
            linearLayout = sweetAlertDialog.findViewById(R.id.loading)
            titleText = sweetAlertDialog.findViewById(R.id.title_text)
            contentText = sweetAlertDialog.findViewById(R.id.content_text)
            confirmButton = sweetAlertDialog.findViewById(R.id.confirm_button)
            cancelButton = sweetAlertDialog.findViewById(R.id.cancel_button)

            linearLayout.setBackgroundResource(R.drawable.sweet_alert_dialog_background)
            titleText.setTextColor(context.resources.getColor(R.color.white, context.theme))
            contentText.setTextColor(context.resources.getColor(R.color.white_70, context.theme))

            if (sweetAlertDialog.alerType != SweetAlertDialog.WARNING_TYPE)
                confirmButton.setBackgroundResource(R.drawable.confirm_button_background)
            cancelButton.setBackgroundResource(R.drawable.cancel_button_background)
        }


        fun onHashNumber(integer: Int): String {
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

        /** Get URL Avatar User */
        fun getURLAvatar(avatar: String): String {
            return RestfulAPIService.AVATAR_TODO_URL + avatar
        }

        /** Get URL Image TodoList */
        fun getURLImage(image: String): String {
            return RestfulAPIService.IMG_TODO_URL + image
        }

        /** Count Down Timer Limit Use Code Verification */
        fun elapsedTimeVerificationCode(time: Long): String {
            return "Code verification resend in ... " + DateUtils.formatElapsedTime(time)
                .replace(".", ":")
        }

        /** Send Verification Code From E-mail */
        fun sendVerificationCode(context: Context, user: User, subject: String, code: String) {
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
                        Credentials.EMAIL_SENDER,
                        /** Sender */
                        user.email
                        /** Recipient */
                    )

                    (context as Activity).runOnUiThread {
                        onSuccessSweetLoading("Mail sent successfully")
                    }

                } catch (e: Exception) {
                    onLog("SendEmail", "${e.message}")
                }
            }).start()
        }

        fun sendMailSuccessChangeEmailAddress(user: User) {
            Thread(Runnable {
                try {
                    val sender =
                        GMailSender(
                            Credentials.EMAIL_SENDER,
                            Credentials.PASSWORD_SENDER
                        )
                    sender.sendMail(
                        "Hi, ${user.username}.",
                        "Your email has been updated to the email address used with your TodoList account. " +
                                "The previous email address is ${user.avatar} and the new address is ${user.email}. " +
                                "If there is an error changing your email address, please contact your Workspace Admin. " +
                                "Thank you!",
                        Credentials.EMAIL_SENDER,
                        /** Sender */
                        user.email
                        /** Recipient */
                    )
                } catch (e: Exception) {
                    onLog("SendEmail", "${e.message}")
                }
            }).start()
        }

        /** Get Current MAC Address */
        private fun getCurrentMacAddress(): String {
            val networkInterfaceList: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            var stringMacAdd = ""

            for (networkInterface: NetworkInterface in networkInterfaceList) {
                if (networkInterface.name.contains("wlan0")) {
                    for (i in networkInterface.hardwareAddress.indices) {
                        var stringMacByte =
                            Integer.toHexString(networkInterface.hardwareAddress[i].toInt())

                        if (stringMacByte.length == 1)
                            stringMacByte = "0$stringMacByte"

                        stringMacAdd = stringMacAdd + stringMacByte.toUpperCase() + ":"
                    }
                    break
                }
            }

            val macAddress = stringMacAdd.substring(0, stringMacAdd.length - 1)
            val result = if (macAddress.contains("FFFFFF")) macAddress.replace(
                "FFFFFF",
                ""
            ) else macAddress

            onLog("YourMacAddress", "MacAddress : $result")

            return result
        }

        fun onStartCheckSelfMACAddress(context: Context) {
            timer = Timer()
            timer.schedule(object : TimerTask(){
                override fun run() {
                    checkMacAddress(context, timer)
                }
            }, 0 , 5000)
        }

        fun onStopCheckSelfMacAddress(){
            timer.cancel()
        }

        private fun checkMacAddress(context: Context, timer: Timer) {
            RestfulAPIService.requestMethod()
                .getMacAddress(ManagerPreferences.getUID(context))
                .enqueue(
                    object : Callback<RestfulAPIResponse> {
                        override fun onResponse(
                            call: Call<RestfulAPIResponse>,
                            response: Response<RestfulAPIResponse>
                        ) {
                            if (response.isSuccessful) {
                                if (getCurrentMacAddress() != response.body()!!.logUsers.mac_address) {
                                    /** When Mac Address Not Equal, Stop Timer */
                                    val sweet =
                                        SweetAlertDialog(
                                            context,
                                            SweetAlertDialog.WARNING_TYPE
                                        )
                                    sweet.titleText = "Oops!"
                                    sweet.contentText =
                                        "Your account already login in another device"
                                    sweet.setConfirmClickListener {
                                        sweet.dismissWithAnimation()
                                        ManagerPreferences.clearUserPreferences(context)
                                        val i = Intent(context, Login::class.java)
                                        i.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(i)
                                        (context as Activity).finish()
                                    }
                                    sweet.show()
                                    initCustomSweetAlertDialog(context, sweet)

                                    timer.cancel()
                                }
                            }
                            onLog("checkSelfMacAddress", response)
                        }

                        override fun onFailure(
                            call: Call<RestfulAPIResponse>,
                            t: Throwable
                        ) {
                            onLog("checkSelfMacAddress", "${t.message}")
                        }
                    })
        }

        /** Create Log User */
        fun onCreateLogUser(uid: Int, action: String) {
            RestfulAPIService.requestMethod().createLogUser(uid, getCurrentMacAddress(), action)
                .enqueue(
                    object : Callback<RestfulAPIResponse> {
                        override fun onResponse(
                            call: Call<RestfulAPIResponse>,
                            response: Response<RestfulAPIResponse>
                        ) {
                            onLog("createLogUser", response)
                        }

                        override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                            onLog("createLogUser", "${t.message}")
                        }
                    })
        }

        /** Get Response Error Body, when Response Code == 400 */
        fun getErrorBody(response: Response<RestfulAPIResponse>): RestfulAPIResponse? {
            return Gson().fromJson(
                response.errorBody()!!.string(), RestfulAPIResponse::class.java
            )
        }

        fun onLog(tag: String, response: Response<RestfulAPIResponse>) {
            Log.d(tag, "Code      : ${response.code()}")
            Log.d(tag, "Message   : ${response.message()}")
            Log.d(tag, "Body      : ${response.body()}")
            Log.d(tag, "ErrorBody : ${response.errorBody()}")
            Log.d(tag, "============================================")
        }

        fun onLog(tag: String, message: String) {
            Log.d(tag, message)
        }

    }
}