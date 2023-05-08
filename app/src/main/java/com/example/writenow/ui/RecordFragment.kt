package com.example.writenow.ui

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
import com.example.writenow.model.byteArrayToRecordModel
import com.example.writenow.apiManager.RecordApiManager
import java.io.FileOutputStream
import java.util.*
import kotlin.properties.Delegates

class RecordFragment: BaseFragment<FragmentRecordBinding>(R.layout.fragment_record) {
    private val REQUEST_RECORD_AUDIO_PERMISSION = 100
    private var record_state:Boolean = false
    val apiManager = RecordApiManager.getInstance(context)
    private lateinit var audioRecord: AudioRecord
    private lateinit var audioData: ByteArray
    private var readBytes by Delegates.notNull<Int>()

    override fun initDataBinding() {
        super.initDataBinding()
    }

    override fun initAfterBinding() {
        super.initAfterBinding()

//        binding.tvTurnCmd.setOnClickListener {
//            navController.navigate(R.id.action_textFragment_to_cmdFragment)
//        }

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

                startRecord()
            }
        }
    }

    private fun startRecord() {
        // 오디오 포맷 설정
        val audioSource = MediaRecorder.AudioSource.MIC
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (context?.let { ContextCompat.checkSelfPermission(it, android.Manifest.permission.RECORD_AUDIO) }
            == PackageManager.PERMISSION_GRANTED) {
            // 권한이 이미 부여되어 있습니다.
            // 여기서 API를 호출합니다.
        } else {
            // 권한이 부여되어 있지 않습니다. 권한 요청 대화상자를 표시하여 권한을 요청합니다.
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_PERMISSION
                )
            }
        }
        audioRecord = AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize)

        // 녹음 시작
        audioData = ByteArray(bufferSize)
        audioRecord.startRecording()
        readBytes = audioRecord.read(audioData, 0, bufferSize)
    }

    private fun completeRecord(){

        // 녹음 종료
        audioRecord.stop()
        audioRecord.release()

        // 녹음된 데이터 반환
        // val result = audioData.copyOfRange(0, readBytes)
        val result = byteArrayToRecordModel(audioData, readBytes)
        Log.d("resultt","apiManager: ${apiManager}, 녹음 완료")
        //apiManager?.getData(result)
        //apiManager?.getTest()
        apiManager?.postTest()

        //saveAudioDataToFile(audioData)
    }

    private fun saveAudioDataToFile(data: ByteArray) {
        // 저장할 파일 경로 지정
        val filename: String = Date().time.toString()+".wav"
        // ${context?.filesDir?.absolutePath}/audio_file.wav
        val filePath = "${context?.filesDir?.absolutePath}/" + filename

        Log.d("filePathh",filePath.toString())

        // FileOutputStream을 사용하여 파일에 데이터를 저장
        FileOutputStream(filePath).use { outStream ->
            outStream.write(data)
        }

        playAudioFile(filePath)
    }

    private fun playAudioFile(filePath: String) {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(filePath)
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
        }
        mediaPlayer.setOnErrorListener { mp, what, extra ->
            Log.e("MediaPlayer", "Error occurred: what = $what, extra = $extra")
            false // 이벤트가 처리되었는지 여부를 반환합니다.
        }
        mediaPlayer.prepareAsync()
    }
}