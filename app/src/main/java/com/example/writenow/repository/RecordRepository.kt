package com.example.writenow.repository

import com.example.writenow.api.RecordService
import com.example.writenow.model.RecordModel
import com.example.writenow.model.ResultModel
import retrofit2.Call

class RecordRepository(private val recordService: RecordService) {
    fun getData(d: RecordModel): Call<ResultModel> = recordService.postRecord(d)
}