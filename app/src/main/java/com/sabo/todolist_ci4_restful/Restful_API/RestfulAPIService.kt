package com.sabo.todolist_ci4_restful.Restful_API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RestfulAPIService {
    companion object {
        private var retrofit: Retrofit? = null

        private const val URL = "http://192.168.1.6/Restful-API/todolist-ci4-restful/public/"
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