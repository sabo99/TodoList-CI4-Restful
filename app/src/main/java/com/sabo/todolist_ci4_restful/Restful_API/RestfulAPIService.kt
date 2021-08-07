package com.sabo.todolist_ci4_restful.Restful_API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RestfulAPIService {
    companion object {
        private var retrofit: Retrofit? = null

        private const val BASE_URL = "http://192.168.1.5"
        private const val DIR = "Restful-API/todolist-ci4-restful/public"

        private const val URL = "$BASE_URL/$DIR/"
        const val IMG_TODO_URL = "${URL}assets/uploads/todolist/"
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