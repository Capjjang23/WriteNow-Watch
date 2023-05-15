package com.example.writenow.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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
import java.util.*
import kotlin.properties.Delegates

class RecordTestFragment : BaseFragment<FragmentRecordBinding>(R.layout.fragment_record) {
    private var record_state:Boolean = false
    val apiManager = RecordApiManager.getInstance(context)
    private lateinit var audioRecord: AudioRecord
    private lateinit var audioData: ByteArray
    private var bufferSize by Delegates.notNull<Int>()
    private var readBytes: Int = 0
    private lateinit var recordingThread: Thread

    override fun initAfterBinding() {
        super.initAfterBinding()

        // 버튼 누를시 녹음 시작, 빨간 배경으로 변경
        binding.btnRecord.setOnClickListener {
            // 녹음 진행중이었을시 fragment 전환
            if (record_state) {
                record_state = false
                completeRecord()
                setFragmentResult(
                    "recordResult",
                    bundleOf("result" to binding.tvResultingRecord.text)
                )
                navController.navigate(R.id.action_recordFragment_to_showResultFragment)
            } else {
                record_state = true

                binding.btnRecord.backgroundTintList = when (record_state) {
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

                if (record_state) {
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
    private fun startRecording() {
        val audioSource = MediaRecorder.AudioSource.MIC
        val sampleRate = 22050
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        audioRecord = AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize)
        audioData = ByteArray(bufferSize)

        audioRecord.startRecording()
        recordingThread = Thread {
            while (record_state) {
                audioRecord.read(audioData, 0, bufferSize)
                Log.d("Threading","쓰레드 실행중..")
            }
        }

        recordingThread.start()
    }

    private fun processRecordedData(data: ByteArray, readBytes: Int) {
        // 원하는 처리 로직을 여기에 구현합니다.
        // 이 예시에서는 녹음된 데이터를 로그로 출력합니다.
        Log.d("RecordFragment", "Recorded Data: ${Arrays.toString(data)}, Read Bytes: $readBytes")
    }
    private fun completeRecord() {
        audioRecord.stop()
        audioRecord.release()

        // ByteArray 변수를 서버로 전송
        val result = RecordModel(audioData)
        Log.d("test!", result.toString())
        //apiManager?.getData(result)
        //apiManager?.postTest(result)
    }
}