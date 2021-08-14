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
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.sabo.todolist_ci4_restful.Helper.Callback.EventOnRefresh
import com.sabo.todolist_ci4_restful.Helper.Callback.FileUtilsCallback
import com.sabo.todolist_ci4_restful.Helper.Callback.KeyStore
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Helper.SharedPreference.ManagerPreferences
import com.sabo.todolist_ci4_restful.Model.Todo
import com.sabo.todolist_ci4_restful.R
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIResponse
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIService
import com.sabo.todolist_ci4_restful.databinding.SweetAlertDialogEditTodoBinding
import com.theartofdev.edmodo.cropper.CropImage
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class EditTodo : AppCompatActivity() {

    private lateinit var binding: SweetAlertDialogEditTodoBinding
    private lateinit var sweetAlertDialog: SweetAlertDialog
    private lateinit var todo: Todo
    private var uid = 0
    private var filePath: String = ""

    override fun onResume() {
        super.onResume()
        ManagerCallback.onStartCheckSelfMACAddress(this)
    }

    override fun onStop() {
        super.onStop()
        ManagerCallback.onStopCheckSelfMacAddress()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_null)

        val view =
            LayoutInflater.from(this).inflate(R.layout.sweet_alert_dialog_edit_todo, null)
        initViews(view)
    }


    private fun initViews(view: View) {

        uid = ManagerPreferences.getUID(this)
        todo = intent.getParcelableExtra("todo")

        sweetAlertDialog = SweetAlertDialog(this)
        binding = SweetAlertDialogEditTodoBinding.bind(view)

        sweetAlertDialog.setCancelable(false)
        sweetAlertDialog.isShowCancelButton
        sweetAlertDialog.cancelText = "Cancel"
        sweetAlertDialog.confirmText = "Update"
        sweetAlertDialog.setOnShowListener {

            Glide.with(this).load(ManagerCallback.getURLImage(todo.image)).into(binding.ivImage)
            binding.etTitle.setText(todo.title)
            binding.etDesc.setText(todo.desc)

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
            val sweet = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            sweet.setCancelable(false)
            sweet.titleText = "Update Todo"
            sweet.contentText = "Are you sure to update this todo?"
            sweet.isShowCancelButton
            sweet.cancelText = "Cancel"
            sweet.confirmText = "Update"
            sweet.setCancelClickListener { sweet.dismissWithAnimation() }
            sweet.setConfirmClickListener { onUpdateTodo(sweet) }
            sweet.show()
            sweet.findViewById<Button>(R.id.confirm_button)
                .setBackgroundResource(R.drawable.confirm_button_background)
            ManagerCallback.initCustomSweetAlertDialog(this, sweet)

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
                .getIntent(this@EditTodo)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return when (resultCode) {
                RESULT_OK -> intent?.let { CropImage.getActivityResult(it).uri }
                else -> null
            }
        }
    }

    private val onActivityResultCropImage = registerForActivityResult(cropActivityResultContracts) {
        it?.let { uri ->
            binding.ivImage.setImageURI(uri)
            binding.tilImage.text = ""

            filePath = FileUtilsCallback.getFilePath(this, uri)
        }
    }

    private fun onMultipleRequestPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), KeyStore.MULTIPLE_PERMISSION
        )
    }

    private fun onUpdateTodo(sweet: SweetAlertDialog) {
        val title = binding.etTitle.text.toString()
        val desc = binding.etDesc.text.toString()
        val image = filePath

        when {
            title.isEmpty() -> binding.tilTitle.error = "The title field is required."
            desc.isEmpty() -> binding.tilDesc.error = "The description field is required."
            else -> {
                sweet.dismissWithAnimation()
                onUpdate(Todo(todo.id, uid, title, desc, image, "", ""))
            }
        }
    }

    /**
     * Update TodoList
     * - Title
     * - Description
     */
    private fun onUpdate(todo: Todo) {
        ManagerCallback.onStartSweetLoading(this, "Update todo")

        RestfulAPIService.requestMethod().updateTodo(todo.id, todo.uid, todo.title, todo.desc)
            .enqueue(
                object : Callback<RestfulAPIResponse> {
                    override fun onResponse(
                        call: Call<RestfulAPIResponse>,
                        response: Response<RestfulAPIResponse>
                    ) {
                        when (response.code()) {
                            200 -> {
                                /**
                                 * Update TodoList
                                 * Value : Title, Desc
                                 */
                                if (todo.image.isEmpty())
                                    Handler().postDelayed({
                                        TodoCallback.onFinish(
                                            this@EditTodo,
                                            sweetAlertDialog
                                        )
                                        Toast.makeText(
                                            this@EditTodo,
                                            response.message(),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EventBus.getDefault()
                                            .postSticky(
                                                EventOnRefresh(
                                                    true,
                                                    response.body()!!.todo
                                                )
                                            )
                                        ManagerCallback.onStopSweetLoading()
                                    }, 2000)

                                /**
                                 * Update & Upload TodoList
                                 * Value : Title, Desc, Image
                                 */
                                else
                                    onUpload(todo)

                                ManagerCallback.onCreateLogUser(todo.uid, KeyStore.UPDATE_TODO)
                            }
                            400 -> {
                                ManagerCallback.onStopSweetLoading()
                                val errors = response.body()!!.errorValidation

                                binding.tilTitle.error = errors.title
                                binding.tilDesc.error = errors.desc
                            }
                            500 -> ManagerCallback.onFailureSweetLoading(response.message())
                        }
                        ManagerCallback.onLog("updateTodo", response)
                    }

                    override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                        ManagerCallback.onFailureSweetLoading("Cannot update todo.\n${KeyStore.ON_FAILURE}")
                        ManagerCallback.onLog("updateTodo", "${t.message}")
                    }
                })
    }

    /**
     * Update & Upload TodoList
     * - Title
     * - Description
     * - Image
     */
    private fun onUpload(todo: Todo) {
        val file = File(todo.image)
        val fileName = FileUtilsCallback.getFileName(Uri.parse(todo.image))
        val fileBody = RequestBody.create(MediaType.parse("image/*"), file)
        val imagePart = MultipartBody.Part.createFormData("image", fileName, fileBody)

        RestfulAPIService.requestMethod().uploadImage(todo.id, imagePart).enqueue(object :
            Callback<RestfulAPIResponse> {
            override fun onResponse(
                call: Call<RestfulAPIResponse>,
                response: Response<RestfulAPIResponse>
            ) {
                when (response.code()) {
                    200 -> {
                        Handler().postDelayed({
                            ManagerCallback.onStopSweetLoading()
                            TodoCallback.onFinish(
                                this@EditTodo,
                                sweetAlertDialog
                            )
                            Toast.makeText(
                                this@EditTodo,
                                response.message(),
                                Toast.LENGTH_SHORT
                            ).show()
                            EventBus.getDefault()
                                .postSticky(EventOnRefresh(true, response.body()!!.todo))
                        }, 2000)
                    }
                    400 -> binding.tilImage.text = ManagerCallback.getErrorBody(response)!!.message
                    500 -> ManagerCallback.onFailureSweetLoading(response.message())
                }
                ManagerCallback.onLog("uploadTodo", response)
            }

            override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                ManagerCallback.onLog("uploadTodo", "${t.message}")
                ManagerCallback.onFailureSweetLoading("Cannot update todo.\n${KeyStore.ON_FAILURE}")
            }

        })
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == KeyStore.MULTIPLE_PERMISSION) {
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