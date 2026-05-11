package com.dp.truning.ui.fragments.home

import android.content.Context.MODE_PRIVATE
import android.app.ActivityManager
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import com.dp.truning.R
import com.dp.truning.domain.model.TunerDisplayMode
import com.dp.truning.domain.model.TunerSettings
import com.dp.truning.domain.model.TuningSensitivity
import com.dp.truning.databinding.FragmentHomeBinding
import com.dp.truning.ui.base.BaseFragment
import com.dp.truning.ui.activitys.MainActivity
import com.dp.truning.util.AudioRecordTarsosInputStream
import com.dp.truning.util.GuitarTone
import com.dp.truning.util.MicrophonePermissionHelper
import com.dp.truning.util.TunerMath
import com.dp.truning.util.toast
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sin

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    companion object {
        private const val PREFS_NAME = "tuner_permission_prefs"
        private const val KEY_HAS_SHOWN_MIC_PERMISSION_GUIDE = "has_shown_mic_permission_guide"
    }

    private var audioDispatcher: AudioDispatcher? = null
    private var audioThread: Thread? = null
    private val tonePreviewPlayer = TonePreviewPlayer()

    private var centRange = 12f
    private val pointerRange = 50f
    private var smoothedPitch: Float? = null

    /**
     * 在视图创建完成后绑定界面状态与交互。
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.page = this
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel
        initView()
    }

    /**
     * 在界面恢复可见时刷新当前状态。
     */
    override fun onResume() {
        super.onResume()
        viewModel.refreshTunerSettings()
        applyTunerSettings()
        if (!shouldSkipAutoEntry()) {
            handleTunerEntry()
        }
    }

    /**
     * 在界面进入暂停前停止正在进行的工作。
     */
    override fun onPause() {
        stopTuner()
        tonePreviewPlayer.stop()
        super.onPause()
    }

    /**
     * 处理 init view 相关逻辑。
     */
    private fun initView() {
        binding.tvTip.text = "准备调音"
        binding.tvTip.setTextColor(Color.BLACK)
        binding.tvPitchStatus.text = "音准：等待检测"
        binding.tvPitchStatus.setTextColor(Color.BLACK)
        updatePitchPointer(0f)
        bindStringKnobs()
        viewModel.selectedIndex.observe(viewLifecycleOwner) { selectedIndex ->
            updateKnobSelection(selectedIndex)
        }
        viewModel.autoDetectEnabled.observe(viewLifecycleOwner) { isEnabled ->
            updateKnobInteractivity(isEnabled)
            updateAutoDetectSummary(isEnabled)
        }
        updateSelectedString(viewModel.selectedIndex.value ?: 4)
        updateAutoDetectSummary(viewModel.autoDetectEnabled.value == true)
    }

    /**
     * 选择 string。
     */
    fun selectString(view: View) {
        val selectedIndex = view.tag?.toString()?.toIntOrNull() ?: return
        updateSelectedString(selectedIndex)
    }

    /**
     * 绑定 string knobs。
     */
    private fun bindStringKnobs() {
        getStringKnobs().forEachIndexed { index, knob ->
            knob.isClickable = true
            knob.isFocusable = true
            knob.setOnClickListener {
                if (viewModel.autoDetectEnabled.value == true) {
                    return@setOnClickListener
                }
                updateSelectedString(index)
                playSelectedStringTone()
            }
        }
    }

    /**
     * 更新 selected string。
     */
    private fun updateSelectedString(selectedIndex: Int) {
        val selectedString = GuitarTone.standardStringAt(selectedIndex, currentSettings().referenceA4Hz) ?: return
        viewModel.selectedIndex.value = selectedString.index
        viewModel.name.value = selectedString.number
        viewModel.selectedLabel.value = selectedString.label
        updateKnobSelection(selectedString.index)
    }

    /**
     * 更新 knob selection。
     */
    private fun updateKnobSelection(selectedIndex: Int) {
        getStringKnobs().forEachIndexed { index, knob ->
            knob.isSelected = index == selectedIndex
        }
    }

    /**
     * 更新 knob interactivity。
     */
    private fun updateKnobInteractivity(autoDetectEnabled: Boolean) {
        val alpha = if (autoDetectEnabled) 0.55f else 1f
        getStringKnobs().forEach { knob ->
            knob.isEnabled = !autoDetectEnabled
            knob.alpha = alpha
        }
    }

    /**
     * 更新 auto detect summary。
     */
    private fun updateAutoDetectSummary(autoDetectEnabled: Boolean) {
        binding.tvAutoDetectSummary.text =
            if (autoDetectEnabled) {
                getString(R.string.home_auto_detect_summary_on)
            } else {
                getString(R.string.home_auto_detect_summary_off)
            }
    }

    /**
     * 获取 selected string。
     */
    private fun getSelectedString(): GuitarTone.GuitarString {
        val referenceA4 = currentSettings().referenceA4Hz
        return GuitarTone.standardStringAt(viewModel.selectedIndex.value ?: 4, referenceA4)
            ?: GuitarTone.standardStrings(referenceA4)[4]
    }

    /**
     * 获取当前 settings。
     */
    private fun currentSettings(): TunerSettings {
        return viewModel.tunerSettings.value ?: TunerSettings()
    }

    /**
     * 应用 tuner settings。
     */
    private fun applyTunerSettings() {
        val settings = currentSettings()
        centRange = when (settings.sensitivity) {
            TuningSensitivity.HIGH -> 8f
            TuningSensitivity.MEDIUM -> 12f
            TuningSensitivity.LOW -> 16f
        }
        smoothedPitch = null
        updateDisplayModeVisibility(settings.displayMode)
        updateSelectedString(viewModel.selectedIndex.value ?: 4)
    }

    /**
     * 更新 display mode visibility。
     */
    private fun updateDisplayModeVisibility(displayMode: TunerDisplayMode) {
        val pointerVisible = displayMode != TunerDisplayMode.NUMERIC
        binding.tvPitchStatus.visibility = if (pointerVisible) View.VISIBLE else View.GONE
        binding.pitchPointerContainer.visibility = if (pointerVisible) View.VISIBLE else View.GONE
        binding.pitchScaleContainer.visibility = if (pointerVisible) View.VISIBLE else View.GONE
        binding.numericContainer.visibility =
            if (displayMode == TunerDisplayMode.NUMERIC) View.VISIBLE else View.GONE
        binding.tvGaugeHelper.visibility =
            if (displayMode == TunerDisplayMode.GAUGE) View.VISIBLE else View.GONE
    }

    /**
     * 获取 string knobs。
     */
    private fun getStringKnobs(): List<TextView> {
        return listOf(
            binding.knob1,
            binding.knob2,
            binding.knob3,
            binding.knob4,
            binding.knob5,
            binding.knob6
        )
    }

    /**
     * 获取 mic。
     */
    fun getMic(view: View) {
        requestMicrophonePermission(showGrantedToast = true, startTunerAfterGrant = false)
    }

    /**
     * 启动 tuner。
     */
    fun startTuner(view: View) {
        if (MicrophonePermissionHelper.hasPermission(this)) {
            startTunerInternal()
            return
        }
        requestMicrophonePermission(showGrantedToast = false, startTunerAfterGrant = true)
    }

    /**
     * 处理 tuner entry。
     */
    private fun handleTunerEntry() {
        if (!isAdded) {
            return
        }

        if (MicrophonePermissionHelper.hasPermission(this)) {
            startTunerInternal()
            return
        }

        if (!hasShownMicPermissionGuide()) {
            showMicrophonePermissionGuide()
        }
    }

    /**
     * 判断是否需要 skip auto entry。
     */
    private fun shouldSkipAutoEntry(): Boolean {
        return ActivityManager.isRunningInUserTestHarness() ||
            requireActivity().intent.getBooleanExtra(MainActivity.EXTRA_SKIP_HOME_AUTO_ENTRY, false)
    }

    /**
     * 显示 microphone permission guide。
     */
    private fun showMicrophonePermissionGuide() {
        markMicPermissionGuideShown()
        AlertDialog.Builder(requireContext())
            .setTitle("开启麦克风权限")
            .setMessage("调音功能需要麦克风权限，是否现在开启？")
            .setPositiveButton("是") { _, _ ->
                requestMicrophonePermission(showGrantedToast = false, startTunerAfterGrant = true)
            }
            .setNegativeButton("否", null)
            .show()
    }

    /**
     * 请求 microphone permission。
     */
    private fun requestMicrophonePermission(
        showGrantedToast: Boolean,
        startTunerAfterGrant: Boolean
    ) {
        MicrophonePermissionHelper.request(
            this,
            onGranted = {
                if (showGrantedToast) {
                    requireActivity().toast("麦克风权限已获取")
                }
                if (startTunerAfterGrant) {
                    startTunerInternal()
                }
            },
            onDenied = { doNotAskAgain ->
                if (doNotAskAgain) {
                    MicrophonePermissionHelper.openAppSettings(this)
                }
            }
        )
    }

    /**
     * 判断是否已具备 shown mic permission guide。
     */
    private fun hasShownMicPermissionGuide(): Boolean {
        return requireContext()
            .getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getBoolean(KEY_HAS_SHOWN_MIC_PERMISSION_GUIDE, false)
    }

    /**
     * 标记 mic permission guide shown。
     */
    private fun markMicPermissionGuideShown() {
        requireContext()
            .getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAS_SHOWN_MIC_PERMISSION_GUIDE, true)
            .apply()
    }

    /**
     * 启动 tuner internal。
     */
    private fun startTunerInternal() {
        stopTuner()
        smoothedPitch = null

        val sampleRate = 44100
        val bufferSize = 2048
        val overlap = 1024
        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (minBufferSize <= 0) {
            requireActivity().toast("麦克风初始化失败")
            return
        }

        val inputStream = try {
            AudioRecordTarsosInputStream(
                sampleRate = sampleRate,
                requestedBufferSizeInBytes = maxOf(bufferSize * 2, minBufferSize)
            )
        } catch (e: Exception) {
            requireActivity().toast("麦克风初始化失败：${e.message ?: "未知错误"}")
            return
        }

        val dispatcher = AudioDispatcher(inputStream, bufferSize, overlap)
        val pitchHandler = PitchDetectionHandler { result, _ ->
            val pitch = result.pitch
            val probability = result.probability
            val sensitivity = currentSettings().sensitivity

            if (pitch > 0f && probability >= sensitivity.minProbability && isAdded) {
                val smoothed = TunerMath.smooth(smoothedPitch, pitch, sensitivity.smoothingFactor)
                smoothedPitch = smoothed
                requireActivity().runOnUiThread {
                    updateTunerUI(smoothed)
                }
            }
        }

        dispatcher.addAudioProcessor(
            PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.YIN,
                sampleRate.toFloat(),
                bufferSize,
                pitchHandler
            )
        )

        audioDispatcher = dispatcher
        audioThread = Thread(dispatcher, "Audio Dispatcher").also { it.start() }

        binding.tvTip.text = "正在监听..."
        binding.tvTip.setTextColor(Color.BLACK)
    }

    /**
     * 处理 freq to note 相关逻辑。
     */
    private fun freqToNote(freq: Float): String {
        if (freq <= 0f) {
            return "--"
        }

        val notes = arrayOf(
            "C", "C#", "D", "D#", "E", "F",
            "F#", "G", "G#", "A", "A#", "B"
        )

        val midi = (69 + 12 * (ln(freq / 440.0f) / ln(2.0f))).roundToInt()
        val note = notes[((midi % 12) + 12) % 12]
        val octave = midi / 12 - 1
        return "$note$octave"
    }

    /**
     * 处理 calculate cent diff 相关逻辑。
     */
    private fun calculateCentDiff(currentFreq: Float, targetFreq: Float): Float {
        if (currentFreq <= 0f || targetFreq <= 0f) {
            return 0f
        }
        return 1200f * (ln(currentFreq / targetFreq) / ln(2.0f))
    }

    /**
     * 更新 tuner UI。
     */
    private fun updateTunerUI(currentFreq: Float) {
        val settings = currentSettings()
        if (viewModel.autoDetectEnabled.value == true) {
            GuitarTone.findClosestStringIndex(currentFreq, settings.referenceA4Hz)?.let(::updateSelectedString)
        }

        val selectedString = getSelectedString()
        val note = TunerMath.noteName(currentFreq, settings.referenceA4Hz)
        val centDiff = TunerMath.centsOff(currentFreq, selectedString.frequency)
        val modeLabel = if (viewModel.autoDetectEnabled.value == true) "自动识别" else "手动选择"
        val statusLabel: String
        val statusColor: Int

        when {
            abs(centDiff) <= centRange -> {
                statusLabel = "音准"
                statusColor = Color.parseColor("#00C853")
            }

            centDiff < 0 -> {
                statusLabel = "偏低"
                statusColor = Color.parseColor("#FF5722")
            }

            else -> {
                statusLabel = "偏高"
                statusColor = Color.parseColor("#FF9800")
            }
        }

        updatePitchPointer(centDiff)
        binding.tvPitchStatus.text = "音准：$statusLabel"
        binding.tvPitchStatus.setTextColor(statusColor)
        binding.tvPitchPointer.setTextColor(statusColor)
        binding.tvNumericNote.text = note
        binding.tvNumericFrequency.text = String.format(Locale.CHINA, "%.2f Hz", currentFreq)
        binding.tvNumericCents.text = String.format(Locale.CHINA, "%.1f cents", centDiff)
        binding.tvTip.text = String.format(
            Locale.CHINA,
            "模式：%s\n琴弦：%s弦（%s）\n当前频率：%.2f Hz\n当前音名：%s\n目标频率：%.2f Hz\n偏差：%.1f 音分",
            modeLabel,
            selectedString.number,
            selectedString.label,
            currentFreq,
            note,
            selectedString.frequency,
            centDiff
        )
        binding.tvTip.setTextColor(statusColor)
    }

    /**
     * 更新 pitch pointer。
     */
    private fun updatePitchPointer(centDiff: Float) {
        val pointer = binding.tvPitchPointer
        val container = binding.pitchPointerContainer
        if (container.width == 0 || pointer.width == 0) {
            container.post { updatePitchPointer(centDiff) }
            return
        }

        val normalizedOffset = centDiff.coerceIn(-pointerRange, pointerRange) / pointerRange
        val maxOffset = (container.width - pointer.width) / 2f
        pointer.translationX = normalizedOffset * maxOffset
    }

    /**
     * 停止 tuner。
     */
    private fun stopTuner() {
        audioDispatcher?.stop()
        audioDispatcher = null
        audioThread?.interrupt()
        audioThread = null
        smoothedPitch = null
    }

    /**
     * 播放 selected string tone。
     */
    private fun playSelectedStringTone() {
        tonePreviewPlayer.play(getSelectedString().frequency)
    }

    /**
     * 在视图销毁时释放与界面相关的资源。
     */
    override fun onDestroyView() {
        stopTuner()
        tonePreviewPlayer.stop()
        super.onDestroyView()
    }

    private class TonePreviewPlayer(
        private val sampleRate: Int = 44_100,
        private val durationMs: Int = 1_200,
        private val bufferSamples: Int = 1_024
    ) {
        private val lock = Any()
        private var playThread: Thread? = null
        private var audioTrack: AudioTrack? = null

        /**
         * 处理 play 相关逻辑。
         */
        fun play(frequency: Float) {
            if (frequency <= 0f) {
                return
            }
            stop()

            val thread = Thread({
                val totalSamples = sampleRate * durationMs / 1_000
                val fadeSamples = (sampleRate * 0.02f).roundToInt().coerceAtLeast(1)
                val chunk = ShortArray(bufferSamples)
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(bufferSamples * 2)

                @Suppress("DEPRECATION")
                val track = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize,
                    AudioTrack.MODE_STREAM
                )

                synchronized(lock) {
                    audioTrack = track
                }

                val phaseStep = 2 * PI * frequency / sampleRate
                var phase = 0.0
                var writtenSamples = 0

                try {
                    track.play()
                    while (writtenSamples < totalSamples && !Thread.currentThread().isInterrupted) {
                        val chunkSize = minOf(bufferSamples, totalSamples - writtenSamples)
                        for (i in 0 until chunkSize) {
                            val sampleIndex = writtenSamples + i
                            val envelope = when {
                                sampleIndex < fadeSamples -> {
                                    sampleIndex.toDouble() / fadeSamples
                                }

                                sampleIndex >= totalSamples - fadeSamples -> {
                                    (totalSamples - sampleIndex).toDouble() / fadeSamples
                                }

                                else -> 1.0
                            }.coerceIn(0.0, 1.0)

                            val sampleValue = sin(phase) * Short.MAX_VALUE * 0.22 * envelope
                            chunk[i] = sampleValue.toInt().toShort()
                            phase += phaseStep
                        }
                        track.write(chunk, 0, chunkSize)
                        writtenSamples += chunkSize
                    }
                } catch (_: IllegalStateException) {
                } finally {
                    synchronized(lock) {
                        if (audioTrack === track) {
                            audioTrack = null
                        }
                        if (playThread === Thread.currentThread()) {
                            playThread = null
                        }
                    }
                    releaseTrack(track)
                }
            }, "tuner-tone-preview")

            synchronized(lock) {
                playThread = thread
            }
            thread.start()
        }

        /**
         * 处理 stop 相关逻辑。
         */
        fun stop() {
            val thread: Thread?
            val track: AudioTrack?
            synchronized(lock) {
                thread = playThread
                playThread = null
                track = audioTrack
                audioTrack = null
            }
            thread?.interrupt()
            releaseTrack(track)
        }

        /**
         * 处理 release track 相关逻辑。
         */
        private fun releaseTrack(track: AudioTrack?) {
            if (track == null) {
                return
            }
            try {
                track.pause()
                track.flush()
                track.stop()
            } catch (_: IllegalStateException) {
            } finally {
                track.release()
            }
        }
    }
}
