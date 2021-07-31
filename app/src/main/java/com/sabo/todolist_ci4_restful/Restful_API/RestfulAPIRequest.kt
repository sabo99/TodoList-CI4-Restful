package com.sabo.todolist_ci4_restful.Restful_API

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface RestfulAPIRequest {

    /**
     * Create Users
     * (Sign Up)
     */
    @FormUrlEncoded
    @POST("api/users")
    fun signUp(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("password_confirm") password_confirm: String
    ): Call<RestfulAPIResponse>


    /**
     * Sign In
     */
    @FormUrlEncoded
    @POST("api/users/auth")
    fun signIn(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RestfulAPIResponse>


    /**
     * Forgot Password
     */
    @GET("api/users/{email}/forgot")
    fun forgotPassword(
        @Path("email") email: String
    ): Call<RestfulAPIResponse>


    /**
     * Show User
     */
    @GET("api/users/{uid}")
    fun showUser(
        @Path("uid") uid: Int
    ): Call<RestfulAPIResponse>


    /**
     * Edit User
     */
    @GET("api/users/{uid}/edit")
    fun editUser(
        @Path("uid") uid: Int
    ): Call<RestfulAPIResponse>


    /**
     * CheckEmailExists for Update Email
     */
    @GET("api/users/{email}/check")
    fun checkEmailExist(
        @Path("email") email: String
    ): Call<RestfulAPIResponse>


    /**
     * Update User
     */
    @FormUrlEncoded
    @PUT("api/users/{uid}")
    fun updateUser(
        @Path("uid") uid: Int,
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("two_factor_auth") two_factor_auth: Int,
    ): Call<RestfulAPIResponse>


    /**
     * Upload Avatar
     */
    @Multipart
    @POST("api/users/{uid}/upload")
    fun uploadAvatar(
        @Path("uid") uid: Int,
        @Part avatar: MultipartBody.Part
    ): Call<RestfulAPIResponse>


    /**
     * Delete User
     */
    @DELETE("api/users/{uid}")
    fun deleteUser(
        @Path("uid") uid: Int,
    ): Call<RestfulAPIResponse>


    /**
     *
     * =====================================================
     * =====================================================
     * =====================================================
     *
     */


    /**
     * Create TodoList
     */
    @Multipart
    @POST("api/todolist")
    fun createTodo(
        @Part uid: MultipartBody.Part,
        @Part title: MultipartBody.Part,
        @Part desc: MultipartBody.Part,
        @Part image: MultipartBody.Part
    ): Call<RestfulAPIResponse>


    /**
     * Show TodoList
     */
    @GET("api/todolist/{uid}")
    fun showTodo(
        @Path("uid") uid: Int
    ): Call<RestfulAPIResponse>


    /**
     * Edit TodoList
     */
    @GET("api/todolist/{id}/edit")
    fun editTodo(
        @Path("id") id: Int
    ): Call<RestfulAPIResponse>

    /**
     * Update TodoList
     */
    @FormUrlEncoded
    @PUT("api/todolist/{id}")
    fun updateTodo(
        @Path("id") id: Int,
        @Field("uid") uid: Int,
        @Field("title") title: String,
        @Field("desc") desc: String,
    ): Call<RestfulAPIResponse>


    /**
     * Upload Image
     */
    @Multipart
    @POST("api/todolist/{id}/upload")
    fun uploadImage(
        @Path("id") id: Int,
        @Part image: MultipartBody.Part
    ): Call<RestfulAPIResponse>


    /**
     * Delete TodoList
     */
    @DELETE("api/todolist/{id}")
    fun deleteTodo(
        @Path("id") id: Int
    ): Call<RestfulAPIResponse>


    /**
     * Delete All TodoList by UID
     */
    @DELETE("api/todolist/{uid}/user")
    fun deleteAllTodo(
        @Path("uid") uid: Int
    ): Call<RestfulAPIResponse>
}