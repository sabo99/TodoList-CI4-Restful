package com.sabo.todolist_ci4_restful.Helper.Callback

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import java.io.*

class FileUtilsCallback {
    companion object {
        fun getFilePath(context: Context, uri: Uri): String {
            val fileName = getFileName(uri)
            val dir = context.getExternalFilesDir(uri.toString())

            if (fileName.isNotEmpty()){
                val copyFile = File(dir.toString() + File.separator + fileName)
                copy(context, uri, copyFile)
                return copyFile.absolutePath
            }
            return null.toString()
        }

        fun getFileName(uri: Uri): String {
            var fileName = ""
            try {
                val path = uri.path
                fileName = path?.substring(path.lastIndexOf('/') + 1) ?: "unknown"
            } catch (e: Exception){
                e.printStackTrace()
            }

            return fileName
        }

        private fun copy(context: Context, uri: Uri, copyFile: File) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return
                val outputStream: OutputStream = FileOutputStream(copyFile)
                copyStream(inputStream, outputStream)
                inputStream.close()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        private fun copyStream(inputStream: InputStream, outputStream: OutputStream): Int {
            val buffer_size = 1024 * 10240
            val buffer = ByteArray(buffer_size)

            val `in` = BufferedInputStream(inputStream, buffer_size)
            val out = BufferedOutputStream(outputStream, buffer_size)
            var count = 0
            var nC: Int
            try {
                while (`in`.read(buffer, 0, buffer_size).also { nC = it } != -1) {
                    out.write(buffer, 0, nC)
                    count += nC
                }
                out.flush()
            } finally {
                try {
                    out.close()
                } catch (e: IOException) {
                    Log.e(e.message, e.toString())
                }
                try {
                    `in`.close()
                } catch (e: IOException) {
                    Log.e(e.message, e.toString())
                }
            }
            return count
        }
    }
}