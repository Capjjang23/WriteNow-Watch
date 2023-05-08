package com.example.writenow.api

import com.example.writenow.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RecordService {
    @GET("/posts/1")
    fun getTest(): Call<TestGetModel>

    @POST("/posts")
    fun postTest(@Body postData: TestModel): Call<PostTestModel>

    @POST("/process_audio")
    fun postRecord(@Body recordData: RecordModel): Call<ResultModel>
}