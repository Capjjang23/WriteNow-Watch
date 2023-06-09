package com.example.writenow.apiManager

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.writenow.api.RecordService
import com.example.writenow.model.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration
import java.time.LocalDateTime

class RecordApiManager {
    private var retrofit: Retrofit? = null
    private var retrofitService: RecordService? = null
    val _resultLivedata: MutableLiveData<String> =
        MutableLiveData()
    val resultLivedata: LiveData<String>
        get() = _resultLivedata

    companion object {  // DCL 적용한 싱글톤 구현
        var instance: RecordApiManager? = null
        fun getInstance(context: Context?): RecordApiManager? {
            if (instance == null) {
                @Synchronized
                if (instance == null)
                    instance = RecordApiManager()
            }
            return instance
        }
    }

    init {
        // http://192.168.47.145:8000
        // https://jsonplaceholder.typicode.com
        retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.47.145:8000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        Log.d("retrofitt","init")
        retrofitService = retrofit?.create(RecordService::class.java)
    }

    fun postTest(postData: RecordModel){
        val resultData: Call<PostTestModel>? = retrofitService?.postTest(postData)
        resultData?.enqueue(object : Callback<PostTestModel> {
            override fun onResponse(
                call: Call<PostTestModel>,
                response: Response<PostTestModel>
            ) {
                if (response.isSuccessful) {
                    val result: PostTestModel = response.body()!!
                    Log.d("resultt", result.toString())
                    //EventBus.getDefault().post(GetDataEvent(resultData))
                } else {
                    //EventBus.getDefault().post(GetDataEvent(null))
                    Log.d("resultt", "실패코드_${response.code()}")
                }
            }

            override fun onFailure(call: Call<PostTestModel>, t: Throwable) {
                t.printStackTrace()
                //EventBus.getDefault().post(GetDataEvent(null))
                Log.d("resultt","통신 실패")
            }
        })
    }


    fun getTest(){
        val resultData: Call<TestGetModel>? = retrofitService?.getTest()
        resultData?.enqueue(object : Callback<TestGetModel> {
            override fun onResponse(
                call: Call<TestGetModel>,
                response: Response<TestGetModel>
            ) {
                if (response.isSuccessful) {
                    val result: TestGetModel = response.body()!!
                    Log.d("resultt", result.toString())
                    //EventBus.getDefault().post(GetDataEvent(resultData))
                } else {
                    //EventBus.getDefault().post(GetDataEvent(null))
                    Log.d("resultt", "실패")
                }
            }

            override fun onFailure(call: Call<TestGetModel>, t: Throwable) {
                t.printStackTrace()
                //EventBus.getDefault().post(GetDataEvent(null))
                Log.d("resultt","통신 실패")
            }
        })
    }

    fun getData(recordData:RecordModel, previous:LocalDateTime) {
        val resultData: Call<ResultModel>? = retrofitService?.postRecord(recordData)
        resultData?.enqueue(object : Callback<ResultModel> {
            override fun onResponse(
                call: Call<ResultModel>,
                response: Response<ResultModel>
            ) {
                if (response.isSuccessful) {
                    val result:ResultModel = response.body()!!
                    Log.d("resultt", resultLivedata.toString())
                    if (_resultLivedata.value==null)
                        _resultLivedata.postValue(result.predicted_alphabet)
                    else
                        _resultLivedata.postValue(_resultLivedata.value+result.predicted_alphabet)
                    val now = LocalDateTime.now()
                    val duration = Duration.between(previous, now)
                    val hours = duration.toHours()
                    val minutes = duration.toMinutes() % 60
                    val seconds = duration.seconds % 60

                    println("시간 차: $hours 시간 $minutes 분 $seconds 초")
                    //EventBus.getDefault().post(GetDataEvent(resultData))
                } else {
                    //EventBus.getDefault().post(GetDataEvent(null))
                    Log.d("resultt", "실패")
                }
            }

            override fun onFailure(call: Call<ResultModel>, t: Throwable) {
                t.printStackTrace()
                //EventBus.getDefault().post(GetDataEvent(null))
                Log.d("resultt","통신 실패")
            }
        })
    }
}