package com.example.writenow.model

data class RecordModel (
    var recordData:ByteArray,
    val length: Int
)

fun byteArrayToRecordModel(audioData: ByteArray, length: Int): RecordModel {
    return RecordModel(audioData.copyOfRange(0, length), length)
}