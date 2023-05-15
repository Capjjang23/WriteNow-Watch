package com.example.writenow.ui

import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.example.writenow.R
import com.example.writenow.apiManager.RecordApiManager
import com.example.writenow.base.BaseFragment
import com.example.writenow.databinding.FragmentRecordBinding
import com.example.writenow.databinding.FragmentRecordTestBinding
import com.example.writenow.model.RecordModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

import java.io.IOException
import java.time.LocalDateTime
import java.util.*

class RecordFragment : BaseFragment<FragmentRecordBinding>(R.layout.fragment_record) {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording:Boolean = false
    private var fileName = ""
    private var filePath = ""
    private val apiManager = RecordApiManager.getInstance(context)

    override fun initStartView() {
        super.initStartView()

        apiManager?._resultLivedata?.postValue("")
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun initDataBinding() {
        super.initDataBinding()

        apiManager?.resultLivedata?.observe(viewLifecycleOwner) {
            binding.tvResultingRecord.text = it
            startRecording()
        }

        // 녹음 시작 버튼
        binding.btnRecord.setOnClickListener {
            // 녹음 진행중이었을시 fragment 전환
            if (isRecording) {
                isRecording = false
                stopRecording()
                setFragmentResult(
                    "recordResult",
                    bundleOf("result" to binding.tvResultingRecord.text)
                )
                navController.navigate(R.id.action_recordFragment_to_showResultFragment)
            } else {
                isRecording = true

                binding.btnRecord.backgroundTintList = when (isRecording) {
                    false -> ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.mainYellow
                        )
                    )
                    true -> ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.mainRed
                        )
                    )
                }

                if (isRecording) {
                    binding.tvResultingRecord.visibility = View.VISIBLE
                    binding.tvInfoStartRecord.visibility = View.INVISIBLE
                } else {
                    binding.tvResultingRecord.visibility = View.VISIBLE
                    binding.tvInfoStartRecord.visibility = View.INVISIBLE
                }

                // 권한 부여 여부
                val isEmpower = ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED

                // 권한 부여 되지 않았을경우
                if (isEmpower) {
                    empowerRecordAudioAndWriteReadStorage()
                    // 권한 부여 되었을 경우
                } else {
                    startRecording()
                }
            }
        }
    }


    // 레코딩, 파일 읽기 쓰기 권한부여
    private fun empowerRecordAudioAndWriteReadStorage(){
        val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(context as Activity, permissions,0)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startRecording() {
        //fileName = Date().time.toString()+".aac"
        fileName = "temp.acc"
        filePath = Environment.getExternalStorageDirectory().absolutePath + "/Download/" + fileName

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS) // or MediaRecorder.OutputFormat.MPEG_4
            setOutputFile(filePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC) // or MediaRecorder.AudioEncoder.DEFAULT
            setAudioSamplingRate(22050) // set the desired sampling rate
            setAudioEncodingBitRate(320000)
            setMaxDuration(1500) // Set the maximum duration to 1.5 seconds

            setOnInfoListener { _, what, _ ->
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    mediaRecorder?.apply {
                        stop()
                        release()
                    }
                    mediaRecorder = null

                    // 서버 전송
                    val previous = LocalDateTime.now()
                    val byteArray = mediaRecorderToByteArray(fileName)
                    val recordModel = byteArray?.let { RecordModel(it) }
                    if (recordModel != null) {
                        apiManager?.getData(recordModel, previous)
                        Log.d("sendFile", "MediaRecorder: $mediaRecorder, 이름: $fileName")
                    }
                }
            }

            try {
                prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            start()
            fileName = filePath
        }
    }

    private fun stopRecording(){
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

//        mediaRecorder?.apply {
//            stop()
//            release()
//        }
//        mediaRecorder = null
//
//        // 서버 전송
//        val previous = LocalDateTime.now()
//        val byteArray = mediaRecorderToByteArray(fileName)
//        val recordModel = byteArray?.let { RecordModel(it) }
//        if (recordModel != null) {
//            apiManager?.getData(recordModel, previous)
//            Log.d("sendFile", "이름: $fileName, 경로: $filePath")
//        }
    }

    private fun mediaRecorderToByteArray(outputFile: String): ByteArray? {
        val file = File(outputFile)
        if (!file.exists()) {
            return null
        }

        val inputStream = FileInputStream(file)
        val buffer = ByteArrayOutputStream()

        inputStream.use { input ->
            buffer.use { output ->
                val data = ByteArray(1024)
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    output.write(data, 0, count)
                }
                output.flush()
            }
        }
        return buffer.toByteArray()
    }
}
    