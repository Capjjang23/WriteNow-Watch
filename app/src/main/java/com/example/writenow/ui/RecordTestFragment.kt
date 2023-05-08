package com.example.writenow.ui

import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.setContentView
import com.example.writenow.R
import com.example.writenow.base.BaseFragment
import com.example.writenow.databinding.FragmentRecordTestBinding
import java.io.File
import java.io.FileInputStream

import java.io.IOException
import java.util.*

class RecordTestFragment : BaseFragment<FragmentRecordTestBinding>(R.layout.fragment_record_test) {
    private val userVersion:Int = Build.VERSION.SDK_INT
    private lateinit var mediaRecorder:MediaRecorder
    //private lateinit var outputPath: File
    private lateinit var outputPath:String
    private var recorder: MediaRecorder? = null
    private var state:Boolean = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun initDataBinding() {
        super.initDataBinding()

        val startBtn = binding.btnStart
        val stopBtn = binding.btnStop

        // 녹음 시작 버튼
        startBtn.setOnClickListener {

            // 권한 부여 여부
            val isEmpower = ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

            // 권한 부여 되지 않았을경우
            if (isEmpower) {
                empowerRecordAudioAndWriteReadStorage()
                // 권한 부여 되었을 경우
            } else {
                startRecording()
            }
            Log.d("record","실행")
        }

        // 녹음 중지 버튼
        stopBtn.setOnClickListener {
            stopRecording()
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
    private fun startRecording(){
        Log.d("record","?")

        val filename: String = Date().time.toString()+".m4a"
        //outputPath = File.createTempFile(filename, null, context?.cacheDir)
        outputPath = Environment.getExternalStorageDirectory().absolutePath+"/Download/"+filename

        //val filename: String = Date().time.toString() + ".m4a"
        // val fileName = "recording_${System.currentTimeMillis()}.m4a"
        //val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        //val file = File.createTempFile(filename, filename)

        if (userVersion>=Build.VERSION_CODES.S)
            mediaRecorder = MediaRecorder(requireContext())
        else if (userVersion>=Build.VERSION_CODES.R)
            mediaRecorder = MediaRecorder()

        recorder = mediaRecorder.apply {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            // mediaRecorder.setOutputFile(outputPath)
            Log.d("record_success_path", outputPath.toString())

            try {
                mediaRecorder.prepare()
                mediaRecorder.start()
                state = true
                Toast.makeText(requireContext(),"녹음이 시작되었습니다.",Toast.LENGTH_SHORT).show()
            } catch (e: IllegalStateException){
                e.printStackTrace()
                Toast.makeText(requireContext(),"녹음 실패",Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Log.d("record_fail_ioexception", e.toString())
                Toast.makeText(requireContext(),"녹음 실패 io",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecording(){
        if(state) {
            mediaRecorder.apply {
                mediaRecorder.stop()
                mediaRecorder.reset()
                mediaRecorder.release()
                state = false
                Toast.makeText(requireContext(), "녹음이 되었습니다.", Toast.LENGTH_SHORT).show()
                Log.d("record_success_path", outputPath.toString())
            }
        }else{
            Toast.makeText(requireContext(), "녹음 상태가 아님", Toast.LENGTH_SHORT).show()
        }
        recorder = null
    }
}
    