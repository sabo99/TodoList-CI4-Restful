package com.sabo.todolist_ci4_restful.Activity.Profile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.sabo.todolist_ci4_restful.Helper.Callback.EventOnRefresh
import com.sabo.todolist_ci4_restful.Helper.Callback.FileUtilsCallback
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Model.User
import com.sabo.todolist_ci4_restful.R
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIResponse
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIService
import com.sabo.todolist_ci4_restful.databinding.ActivityEditProfileBinding
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class EditProfile : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var user: User
    private var filePath: String = ""


    override fun finish() {
        super.finish()
        binding.chronometer.stop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        user = intent.getParcelableExtra("user")

        Picasso.get().load(user.avatar?.let { ProfileCallback.getURLAvatar(it) })
            .placeholder(R.drawable.ic_round_person).into(binding.civAvatar)

        binding.chronometer.setOnChronometerTickListener {
            val time = SystemClock.elapsedRealtime() - it.base
            val format = DateFormat.format("mm:ss", time)
            it.text = StringBuilder().append(format).append(" elapsed.")
        }
        binding.chronometer.start()


        binding.btnChangeAvatar.setOnClickListener { onCheckPermissions() }
    }

    private fun onCheckPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) + checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                /**
                 *  When Permission Denied
                 */
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                )
                    onMultipleRequestPermission()

                /**
                 * When Permission Granted
                 */
                else
                    onMultipleRequestPermission()
            } else
                onActivityResultCropImage.launch(null)
        } else
            onActivityResultCropImage.launch(null)

    }

    private fun onMultipleRequestPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), ManagerCallback.MULTIPLE_PERMISSION
        )
    }

    private val cropActivityResultContracts = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity()
                .setAspectRatio(16, 9)
                .getIntent(this@EditProfile)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return when (resultCode) {
                RESULT_OK -> intent?.let { CropImage.getActivityResult(it).uri }
                else -> null
            }
        }
    }

    private val onActivityResultCropImage =
        registerForActivityResult(cropActivityResultContracts) {
            it?.let { uri ->
                binding.civAvatar.setImageURI(uri)
                filePath = FileUtilsCallback.getFilePath(this, uri)

                ManagerCallback.onStartSweetLoading(this, "Please wait", "Change avatar")
                Handler().postDelayed({ uploadAvatar() }, 2000)
            }
        }

    private fun uploadAvatar() {
        val file = File(filePath)
        val fileName = FileUtilsCallback.getFileName(Uri.parse(filePath))
        val fileBody = RequestBody.create(MediaType.parse("image/*"), file)
        val avatarPart = MultipartBody.Part.createFormData("avatar", fileName, fileBody)

        RestfulAPIService.requestMethod().uploadAvatar(user.uid, avatarPart)
            .enqueue(object :
                Callback<RestfulAPIResponse> {
                override fun onResponse(
                    call: Call<RestfulAPIResponse>,
                    response: Response<RestfulAPIResponse>
                ) {
                    if (response.isSuccessful) {
                        when (response.body()!!.code) {
                            200 -> {
                                ManagerCallback.onStopSweetLoading()
                                EventBus.getDefault().postSticky(EventOnRefresh(true, ""))
                                Toast.makeText(
                                    this@EditProfile,
                                    response.message(),
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                            400 -> {
                                ManagerCallback.onFailureSweetLoading(response.message())
                            }
                        }
                    } else
                        ManagerCallback.onFailureSweetLoading(response.message())
                }

                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                    ManagerCallback.onFailureSweetLoading("Something wrong with server connection")
                }
            })
    }


}