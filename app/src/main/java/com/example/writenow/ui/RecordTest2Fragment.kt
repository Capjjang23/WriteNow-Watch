package com.example.writenow.ui

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.example.writenow.R
import com.example.writenow.base.BaseFragment
import com.example.writenow.databinding.FragmentRecordBinding
import com.example.writenow.apiManager.RecordApiManager
import com.example.writenow.model.RecordModel
import java.io.*
import java.util.*
import kotlin.properties.Delegates

class RecordTest2Fragment: BaseFragment<FragmentRecordBinding>(R.layout.fragment_record) {
    private var record_state:Boolean = false
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var byteArray= ByteArray(1000)
    private val filename: String = Date().time.toString()+".3gp"
    private val filePath = Environment.getExternalStorageDirectory().absolutePath+"/Download/"+filename

    override fun initAfterBinding() {
        super.initAfterBinding()

        // 버튼 누를시 녹음 시작, 빨간 배경으로 변경
        binding.btnRecord.setOnClickListener {
            // 녹음 진행중이었을시 fragment 전환
            if (record_state) {
                completeRecord()
                setFragmentResult("recordResult", bundleOf("result" to binding.tvResultingRecord.text))
                navController.navigate(R.id.action_recordFragment_to_showResultFragment)
            } else {
                record_state=!record_state

                binding.btnRecord.backgroundTintList = when (record_state) {
                    false -> ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.mainYellow))
                    true -> ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.mainRed))
                }

                if (record_state){
                    binding.tvResultingRecord.visibility = View.VISIBLE
                    binding.tvInfoStartRecord.visibility = View.INVISIBLE
                } else{
                    binding.tvResultingRecord.visibility = View.VISIBLE
                    binding.tvInfoStartRecord.visibility = View.INVISIBLE
                }

                // 권한 부여 여부
                val isEmpower = ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

                // 권한 부여 되지 않았을경우
                if (isEmpower) {
                    empowerRecordAudioAndWriteReadStorage()
                    // 권한 부여 되었을 경우
                } else {
                    startRecording(filePath)
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
    fun startRecording(outputFile: String) {
        if (isRecording) return

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(256000) // 비트레이트 설정
            setAudioSamplingRate(44100) // 샘플링 레이트 설정

            try {
                prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            start()
            isRecording = true
        }
    }

    fun completeRecord() {
        if (!isRecording) return

        mediaRecorder?.apply {
            stop()
            release()
        }

        byteArray = mediaRecorderToByteArray(filePath)!!
        mediaRecorder = null
        isRecording = false

        // 서버 전송
        val recordModel = RecordModel(byteArray)
        val apiManager = RecordApiManager.getInstance(context)
        apiManager?.postTest(recordModel)
        Log.d("resultt",byteArray.contentToString())
    }

    fun mediaRecorderToByteArray(outputFile: String): ByteArray? {
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