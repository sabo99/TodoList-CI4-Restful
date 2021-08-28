package com.sabo.todolist_ci4_restful.Restful_API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RestfulAPIService {
    companion object {
        private var retrofit: Retrofit? = null

//        private const val URL = "http://todolist-ci4-restfulapi.net/"
        private const val URL = "http://192.168.1.8/ci4todolist-restfulapi/public/"
//        private const val URL = "https://sabohao.000webhostapp.com/"
//        private const val URL = "https://sabo.infinityfreeapp.com/"
        const val IMG_TODO_URL = "${URL}assets/uploads/todoList/"
        const val AVATAR_TODO_URL = "${URL}assets/uploads/users/"

        private fun instance(): Retrofit {
            if (retrofit == null)
                retrofit = Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create()).build()
            return retrofit!!
        }

        fun requestMethod(): RestfulAPIRequest {
            return instance().create(RestfulAPIRequest::class.java)
        }
    }
}