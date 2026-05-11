package com.dp.truning.util

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.TarsosDSPAudioInputStream
import java.io.IOException
import kotlin.math.max

class AudioRecordTarsosInputStream(
    sampleRate: Int,
    channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    audioEncoding: Int = AudioFormat.ENCODING_PCM_16BIT,
    audioSource: Int = MediaRecorder.AudioSource.MIC,
    requestedBufferSizeInBytes: Int
) : TarsosDSPAudioInputStream {

    private val frameSize = 2
    private val format = TarsosDSPAudioFormat(sampleRate.toFloat(), 16, 1, true, false)
    private val audioRecord: AudioRecord
    private var started = false
    private var closed = false

    init {
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding)
        require(minBufferSize > 0) { "Invalid microphone buffer size: $minBufferSize" }

        val bufferSizeInBytes = max(requestedBufferSizeInBytes, minBufferSize * 2)
        audioRecord = AudioRecord(
            audioSource,
            sampleRate,
            channelConfig,
            audioEncoding,
            bufferSizeInBytes
        )

        check(audioRecord.state == AudioRecord.STATE_INITIALIZED) {
            "AudioRecord initialization failed"
        }
    }

    /**
     * 跳过指定长度的数据。
     */
    override fun skip(bytesToSkip: Long): Long {
        ensureOpen()
        if (bytesToSkip <= 0) {
            return 0
        }

        val discardBuffer = ByteArray(4096)
        var skipped = 0L
        while (skipped < bytesToSkip) {
            val bytesToRead = minOf(discardBuffer.size.toLong(), bytesToSkip - skipped).toInt()
            val bytesRead = read(discardBuffer, 0, bytesToRead)
            if (bytesRead <= 0) {
                break
            }
            skipped += bytesRead
        }
        return skipped
    }

    /**
     * 从底层输入源读取指定长度的数据。
     */
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        ensureOpen()
        ensureStarted()

        val boundedLength = len - (len % frameSize)
        if (boundedLength <= 0) {
            return 0
        }

        val bytesRead = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioRecord.read(b, off, boundedLength, AudioRecord.READ_BLOCKING)
        } else {
            audioRecord.read(b, off, boundedLength)
        }

        return when (bytesRead) {
            AudioRecord.ERROR_INVALID_OPERATION -> throw IOException("AudioRecord is not ready")
            AudioRecord.ERROR_BAD_VALUE -> throw IOException("AudioRecord received an invalid read request")
            AudioRecord.ERROR_DEAD_OBJECT -> throw IOException("AudioRecord became unavailable")
            else -> bytesRead
        }
    }

    /**
     * 关闭当前资源并释放底层对象。
     */
    override fun close() {
        if (closed) {
            return
        }
        closed = true

        if (started) {
            try {
                audioRecord.stop()
            } catch (_: IllegalStateException) {
            }
        }
        audioRecord.release()
    }

    /**
     * 返回当前输入流使用的音频格式。
     */
    override fun getFormat(): TarsosDSPAudioFormat = format

    /**
     * 返回当前输入流的帧长度信息。
     */
    override fun getFrameLength(): Long = -1L

    /**
     * 确保底层录音器已经开始采集。
     */
    private fun ensureStarted() {
        if (!started) {
            audioRecord.startRecording()
            started = true
        }
    }

    /**
     * 确保当前输入流尚未关闭。
     */
    private fun ensureOpen() {
        check(!closed) { "Audio input stream already closed" }
    }
}
