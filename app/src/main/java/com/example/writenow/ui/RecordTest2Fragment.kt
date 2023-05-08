package com.example.writenow.ui

import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.writenow.R
import com.example.writenow.base.BaseFragment
import com.example.writenow.databinding.FragmentRecordTestBinding

class RecordTest2Fragment:BaseFragment<FragmentRecordTestBinding> (R.layout.fragment_record_test) {
    private val REQUEST_RECORD_AUDIO_PERMISSION = 100

    private fun recordAudio(): ByteArray {
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
        val audioRecord = AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize)

        // 녹음 시작
        val audioData = ByteArray(bufferSize)
        audioRecord.startRecording()
        val readBytes = audioRecord.read(audioData, 0, bufferSize)

        // 녹음 종료
        audioRecord.stop()
        audioRecord.release()

        // 녹음된 데이터 반환
        return audioData.copyOfRange(0, readBytes)
    }
}