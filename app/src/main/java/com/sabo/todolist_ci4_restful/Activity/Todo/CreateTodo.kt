package com.sabo.todolist_ci4_restful.Activity.Todo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.sabo.todolist_ci4_restful.Helper.Callback.EventOnRefresh
import com.sabo.todolist_ci4_restful.Helper.Callback.FileUtilsCallback
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Model.Todo
import com.sabo.todolist_ci4_restful.Model.User
import com.sabo.todolist_ci4_restful.R
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIResponse
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIService
import com.sabo.todolist_ci4_restful.databinding.SweetAlertDialogCreateTodoBinding
import com.theartofdev.edmodo.cropper.CropImage
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class CreateTodo : AppCompatActivity() {

    private lateinit var binding: SweetAlertDialogCreateTodoBinding
    private lateinit var sweetAlertDialog: SweetAlertDialog
    private lateinit var user: User
    private var filePath: String = ""

    override fun onBackPressed() {
        super.onBackPressed()
        TodoCallback.onFinish(this, sweetAlertDialog)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_null)

        val view =
            LayoutInflater.from(this).inflate(R.layout.sweet_alert_dialog_create_todo, null)
        initViews(view)
    }

    private fun initViews(view: View) {
        user = intent.getParcelableExtra("user")

        sweetAlertDialog = SweetAlertDialog(this)
        binding = SweetAlertDialogCreateTodoBinding.bind(view)

        sweetAlertDialog.setCancelable(false)
        sweetAlertDialog.isShowCancelButton
        sweetAlertDialog.cancelText = "Cancel"
        sweetAlertDialog.confirmText = "Create"
        sweetAlertDialog.setOnShowListener {
            onTextWatcher()

            binding.ibClose.setOnClickListener {
                TodoCallback.onFinish(this, sweetAlertDialog)
            }

            binding.ibChangeImage.setOnClickListener { onCheckPermissions() }
        }
        sweetAlertDialog.setCancelClickListener {
            TodoCallback.onFinish(this, sweetAlertDialog)
        }
        sweetAlertDialog.setConfirmClickListener {
            onCreateTodo()
        }
        sweetAlertDialog.show()
        ManagerCallback.initCustomSweetAlertDialog(this, view, sweetAlertDialog)
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

    private val cropActivityResultContracts = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity()
                .setAspectRatio(16, 9)
                .getIntent(this@CreateTodo)
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
                binding.ivImage.setImageURI(uri)
                binding.ivImageTmp.setBackgroundResource(R.drawable.border_image_tmp)
                binding.tilImage.text = ""

                filePath = FileUtilsCallback.getFilePath(this, uri)
            }
        }

    private fun onMultipleRequestPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), ManagerCallback.MULTIPLE_PERMISSION
        )
    }

    private fun onCreateTodo() {
        val title = binding.etTitle.text.toString()
        val desc = binding.etDesc.text.toString()
        val image = filePath

        when {
            image.isEmpty() -> {
                binding.ivImageTmp.setBackgroundResource(R.drawable.border_image_error)
                binding.tilImage.text = "The image is required."
            }
            title.isEmpty() -> binding.tilTitle.error = "The title field is required."
            desc.isEmpty() -> binding.tilDesc.error = "The description field is required."
            else -> {
                ManagerCallback.onStartSweetLoading(this, "Create todo")

                val todo = Todo(0, user.uid, title, desc, image, "", "")

                val uidPart = MultipartBody.Part.createFormData("uid", todo.uid.toString())
                val titlePart = MultipartBody.Part.createFormData("title", todo.title)
                val descPart = MultipartBody.Part.createFormData("desc", todo.desc)

                val file = File(todo.image)
                val fileName = FileUtilsCallback.getFileName(Uri.parse(todo.image))
                val fileBody = RequestBody.create(MediaType.parse("image/*"), file)
                val imagePart = MultipartBody.Part.createFormData("image", fileName, fileBody)

                RestfulAPIService.requestMethod()
                    .createTodo(uidPart, titlePart, descPart, imagePart).enqueue(
                        object : Callback<RestfulAPIResponse> {
                            override fun onResponse(
                                call: Call<RestfulAPIResponse>,
                                response: Response<RestfulAPIResponse>
                            ) {
                                if (response.isSuccessful) {
                                    when (response.body()!!.code) {
                                        201 -> {
                                            Handler().postDelayed({
                                                TodoCallback.onFinish(
                                                    this@CreateTodo,
                                                    sweetAlertDialog
                                                )
                                                Toast.makeText(
                                                    this@CreateTodo,
                                                    response.message(),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                EventBus.getDefault()
                                                    .postSticky(EventOnRefresh(true, ""))
                                                ManagerCallback.onStopSweetLoading()
                                            }, 2000)
                                        }
                                        400 -> {
                                            ManagerCallback.onStopSweetLoading()
                                            val errors = response.body()!!.errorValidation
                                            binding.tilTitle.error = errors.title
                                            binding.tilDesc.error = errors.desc
                                        }
                                        401 -> {
                                            ManagerCallback.onStopSweetLoading()
                                            binding.tilImage.text =
                                                response.body()!!.message
                                        }
                                    }

                                } else
                                    ManagerCallback.onFailureSweetLoading(response.message())

                                Log.d("createTodo-onResponse", response.body().toString())
                            }

                            override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                                ManagerCallback.onFailureSweetLoading("Cannot create todo.\nSomething wrong with server connection")
                                Log.d("createTodo-onFailure", t.message!!)
                            }
                        })
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == ManagerCallback.MULTIPLE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()

                onActivityResultCropImage.launch(null)
            } else if (grantResults.isNotEmpty() && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun onTextWatcher() {
        binding.ibChangeImage.setOnClickListener {
            binding.ivImageTmp.setBackgroundResource(R.drawable.border_image_normal)
            binding.tilImage.text = ""
        }
        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty())
                    binding.tilTitle.error = ""
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        binding.etDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty())
                    binding.tilDesc.error = ""
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

}