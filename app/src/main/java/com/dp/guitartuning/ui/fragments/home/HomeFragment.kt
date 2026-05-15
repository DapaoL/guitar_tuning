package com.dp.guitartuning.ui.fragments.home

import android.app.ActivityManager
import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import com.dp.guitartuning.R
import com.dp.guitartuning.databinding.DialogMicrophonePermissionBinding
import com.dp.guitartuning.databinding.FragmentHomeBinding
import com.dp.guitartuning.domain.model.TunerDisplayMode
import com.dp.guitartuning.domain.model.TunerSettings
import com.dp.guitartuning.domain.model.TuningSensitivity
import com.dp.guitartuning.ui.activitys.MainActivity
import com.dp.guitartuning.ui.base.BaseVmFragment
import com.dp.guitartuning.util.AudioRecordTarsosInputStream
import com.dp.guitartuning.util.GuitarTone
import com.dp.guitartuning.util.MicrophonePermissionHelper
import com.dp.guitartuning.util.TunerMath
import com.dp.guitartuning.util.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

@AndroidEntryPoint
class HomeFragment : BaseVmFragment<FragmentHomeBinding, HomeViewModel>() {

    companion object {
        private const val PREFS_NAME = "tuner_permission_prefs"
        private const val KEY_HAS_SHOWN_MIC_PERMISSION_GUIDE = "has_shown_mic_permission_guide"
    }

    private var audioDispatcher: AudioDispatcher? = null
    private var audioThread: Thread? = null
    private var microphonePermissionDialog: AlertDialog? = null
    private var pendingMicPermissionGrantedToast = false
    private var pendingMicPermissionStartTuner = false
    private val tonePreviewPlayer = TonePreviewPlayer()
    private val pitchPointerUpdateScheduler = PitchPointerUpdateScheduler { centDiff ->
        updatePitchPointer(centDiff)
    }

    private var centRange = 12f
    private val pointerRange = 50f
    private var smoothedPitch: Float? = null

    /**
     * 在视图创建完成后绑定首页状态与交互。
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.page = this
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel
        initView()
    }

    /**
     * 在界面恢复可见时刷新调音配置，并根据权限状态决定是否自动进入监听。
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        viewModel.refreshTunerSettings()
        applyTunerSettings()
        applyKeepScreenOn()
        if (MicrophonePermissionHelper.hasPermission(this)) {
            hidePermissionTip()
        }
        if (!shouldSkipAutoEntry()) {
            handleTunerEntry()
        }
    }

    /**
     * 在界面进入后台前停止监听与试音播放，并清除常亮标记。
     */
    override fun onPause() {
        stopTuner()
        tonePreviewPlayer.stop()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
    }

    /**
     * 初始化首页控件与观察者。
     */
    private fun initView() {
        binding.tvTip.text = getString(R.string.home_status_placeholder)
        binding.tvTip.setTextColor(Color.BLACK)
        binding.tvTip.visibility = View.INVISIBLE
        binding.tvPitchStatus.text = getString(R.string.home_pitch_status_waiting)
        binding.tvPitchStatus.setTextColor(Color.BLACK)
        updatePitchPointer(0f)

        viewModel.selectedIndex.observe(viewLifecycleOwner) { selectedIndex ->
            updateKnobSelection(selectedIndex)
        }

        viewModel.autoDetectEnabled.observe(viewLifecycleOwner) { isEnabled ->
            updateKnobInteractivity(isEnabled)
            updateAutoDetectSummary(isEnabled)
        }

        updateSelectedString(viewModel.selectedIndex.value ?: 4)
        updateAutoDetectSummary(viewModel.autoDetectEnabled.value == true)
        hidePermissionTip()
    }

    /**
     * 隐藏麦克风权限提示。
     */
    private fun hidePermissionTip() {
        binding.tvTip.isClickable = false
        binding.tvTip.visibility = View.INVISIBLE
        binding.tvTip.text = getString(R.string.home_status_placeholder)
        binding.tvTip.setTextColor(Color.BLACK)
    }

    /**
     * 在麦克风权限获取失败后显示可重新申请的提示。
     */
    private fun showPermissionDeniedTip() {
        val actionText = getString(R.string.home_mic_permission_inline_action)
        binding.tvTip.text = getString(R.string.home_mic_permission_inline_prompt, actionText)
        binding.tvTip.setTextColor(Color.BLACK)
        binding.tvTip.paint.isUnderlineText = true
        binding.tvTip.isClickable = true
        binding.tvTip.visibility = View.VISIBLE
    }

    fun requestMicFromPermissionTip(@Suppress("UNUSED_PARAMETER") view: View) {
        requestMicrophonePermission(
            showGrantedToast = true,
            startTunerAfterGrant = false
        )
    }

    /**
     * 手动选择目标琴弦。
     */
    fun selectString(view: View) {
        if (viewModel.autoDetectEnabled.value == true) {
            return
        }
        val selectedIndex = view.tag?.toString()?.toIntOrNull() ?: return
        updateSelectedString(selectedIndex)
        playSelectedStringTone()
    }

    /**
     * 更新当前选中的琴弦。
     */
    private fun updateSelectedString(selectedIndex: Int) {
        val selectedString = GuitarTone.standardStringAt(selectedIndex, currentSettings().referenceA4Hz) ?: return
        viewModel.selectedIndex.value = selectedString.index
        viewModel.name.value = selectedString.number
        viewModel.selectedLabel.value = selectedString.label
        updateKnobSelection(selectedString.index)
    }

    /**
     * 刷新旋钮选中状态。
     */
    private fun updateKnobSelection(selectedIndex: Int) {
        getStringKnobs().forEachIndexed { index, knob ->
            knob.isSelected = index == selectedIndex
        }
    }

    /**
     * 根据是否自动识别调整旋钮可交互状态。
     */
    private fun updateKnobInteractivity(autoDetectEnabled: Boolean) {
        val alpha = if (autoDetectEnabled) 0.55f else 1f
        getStringKnobs().forEach { knob ->
            knob.isEnabled = !autoDetectEnabled
            knob.alpha = alpha
        }
    }

    /**
     * 更新自动识别说明文案。
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
     * 获取当前选中的标准弦信息。
     */
    private fun getSelectedString(): GuitarTone.GuitarString {
        val referenceA4 = currentSettings().referenceA4Hz
        return GuitarTone.standardStringAt(viewModel.selectedIndex.value ?: 4, referenceA4)
            ?: GuitarTone.standardStrings(referenceA4)[4]
    }

    /**
     * 获取当前调音设置。
     */
    private fun currentSettings(): TunerSettings {
        return viewModel.tunerSettings.value ?: TunerSettings()
    }

    /**
     * 根据通用设置应用或清除屏幕常亮标记。
     */
    private fun applyKeepScreenOn() {
        val keepOn = viewModel.getKeepScreenOnEnabled()
        if (keepOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    /**
     * 应用调音配置到首页展示和检测逻辑。
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
     * 根据显示模式切换界面区域可见性。
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
     * 获取六个弦钮控件。
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
     * 手动触发麦克风权限申请。
     */
    fun getMic(@Suppress("UNUSED_PARAMETER") view: View) {
        if (MicrophonePermissionHelper.hasPermission(this)) {
            requireActivity().toast(getString(R.string.home_mic_permission_granted))
            return
        }
        showMicrophonePermissionGuide(showGrantedToast = true, startTunerAfterGrant = false)
    }

    /**
     * 手动开始调音。
     */
    fun startTuner(@Suppress("UNUSED_PARAMETER") view: View) {
        if (MicrophonePermissionHelper.hasPermission(this)) {
            startTunerInternal()
            return
        }
        showMicrophonePermissionGuide(showGrantedToast = false, startTunerAfterGrant = true)
    }

    /**
     * 处理首页自动进入调音的流程。
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
            showMicrophonePermissionGuide(showGrantedToast = false, startTunerAfterGrant = true)
        }
    }

    /**
     * 判断当前是否应跳过自动进入监听。
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun shouldSkipAutoEntry(): Boolean {
        return ActivityManager.isRunningInUserTestHarness() ||
            requireActivity().intent.getBooleanExtra(MainActivity.EXTRA_SKIP_HOME_AUTO_ENTRY, false)
    }

    /**
     * 显示麦克风权限引导弹窗。
     */
    private fun showMicrophonePermissionGuide(
        showGrantedToast: Boolean,
        startTunerAfterGrant: Boolean
    ) {
        if (!isAdded) {
            return
        }

        markMicPermissionGuideShown()
        pendingMicPermissionGrantedToast = showGrantedToast
        pendingMicPermissionStartTuner = startTunerAfterGrant
        val dialogBinding = DialogMicrophonePermissionBinding.inflate(layoutInflater)
        dialogBinding.page = this
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        microphonePermissionDialog = dialog
        dialog.setOnDismissListener {
            if (microphonePermissionDialog === dialog) {
                microphonePermissionDialog = null
            }
        }

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val density = resources.displayMetrics.density
            val dialogWidth = (resources.displayMetrics.widthPixels - (40 * density).roundToInt())
                .coerceAtMost((340 * density).roundToInt())
            dialog.window?.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)
        }
        dialog.show()
    }

    /**
     * 请求麦克风权限，并根据结果更新页面状态。
     */
    fun allowMicrophonePermission(@Suppress("UNUSED_PARAMETER") view: View) {
        microphonePermissionDialog?.dismiss()
        requestMicrophonePermission(
            showGrantedToast = pendingMicPermissionGrantedToast,
            startTunerAfterGrant = pendingMicPermissionStartTuner
        )
    }

    fun skipMicrophonePermission(@Suppress("UNUSED_PARAMETER") view: View) {
        microphonePermissionDialog?.dismiss()
    }

    fun toggleMicrophonePermissionDetails(view: View) {
        val detailText = view.rootView.findViewById<View>(R.id.micPermissionDetailsText) ?: return
        detailText.visibility = if (detailText.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun requestMicrophonePermission(
        showGrantedToast: Boolean,
        startTunerAfterGrant: Boolean
    ) {
        MicrophonePermissionHelper.request(
            this,
            onGranted = {
                if (showGrantedToast) {
                    requireActivity().toast(getString(R.string.home_mic_permission_granted))
                }
                hidePermissionTip()
                if (startTunerAfterGrant) {
                    startTunerInternal()
                }
            },
            onDenied = { doNotAskAgain ->
                requireActivity().toast(getString(R.string.home_mic_permission_required))
                showPermissionDeniedTip()
                if (doNotAskAgain) {
                    MicrophonePermissionHelper.openAppSettings(this)
                }
            }
        )
    }

    /**
     * 判断是否已经展示过麦克风权限引导。
     */
    private fun hasShownMicPermissionGuide(): Boolean {
        return requireContext()
            .getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getBoolean(KEY_HAS_SHOWN_MIC_PERMISSION_GUIDE, false)
    }

    /**
     * 记录麦克风权限引导已展示。
     */
    private fun markMicPermissionGuideShown() {
        requireContext()
            .getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAS_SHOWN_MIC_PERMISSION_GUIDE, true)
            .apply()
    }

    /**
     * 启动音高监听。
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
            requireActivity().toast(getString(R.string.home_mic_init_failed))
            return
        }

        val inputStream = try {
            AudioRecordTarsosInputStream(
                sampleRate = sampleRate,
                requestedBufferSizeInBytes = maxOf(bufferSize * 2, minBufferSize)
            )
        } catch (e: Exception) {
            val reason = e.message ?: getString(R.string.home_unknown_error)
            requireActivity().toast(getString(R.string.home_mic_init_failed_with_reason, reason))
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
                    if (isAdded && view != null) {
                        updateTunerUI(smoothed)
                    }
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
        hidePermissionTip()
    }

    /**
     * 根据当前频率刷新调音界面。
     */
    private fun updateTunerUI(currentFreq: Float) {
        if (view == null) {
            return
        }

        val settings = currentSettings()
        if (viewModel.autoDetectEnabled.value == true) {
            GuitarTone.findClosestStringIndex(currentFreq, settings.referenceA4Hz)?.let(::updateSelectedString)
        }

        val selectedString = getSelectedString()
        val note = TunerMath.noteName(currentFreq, settings.referenceA4Hz)
        val centDiff = TunerMath.centsOff(currentFreq, selectedString.frequency)
        val statusLabel: String
        val statusColor: Int
        when {
            abs(centDiff) <= centRange -> {
                statusLabel = getString(R.string.home_pitch_status_in_tune)
                statusColor = Color.parseColor("#00C853")
            }

            centDiff < 0 -> {
                statusLabel = getString(R.string.home_pitch_status_flat)
                statusColor = Color.parseColor("#FF5722")
            }

            else -> {
                statusLabel = getString(R.string.home_pitch_status_sharp)
                statusColor = Color.parseColor("#FF9800")
            }
        }

        updatePitchPointer(centDiff)
        binding.tvPitchStatus.text = getString(R.string.home_pitch_status_format, statusLabel)
        binding.tvPitchStatus.setTextColor(statusColor)
        binding.tvPitchPointer.setTextColor(statusColor)
        binding.tvNumericNote.text = note
        binding.tvNumericFrequency.text = getString(R.string.home_numeric_frequency_format, currentFreq)
        binding.tvNumericCents.text = getString(R.string.home_numeric_cents_format, centDiff)
        hidePermissionTip()
    }

    /**
     * 按音分偏差更新指针位置。
     */
    private fun updatePitchPointer(centDiff: Float) {
        if (view == null) {
            return
        }

        val pointer = binding.tvPitchPointer
        val container = binding.pitchPointerContainer
        if (container.width == 0 || pointer.width == 0) {
            pitchPointerUpdateScheduler.schedule(
                centDiff = centDiff,
                post = { runnable -> container.post(runnable) },
                remove = { runnable -> container.removeCallbacks(runnable) }
            )
            return
        }

        pitchPointerUpdateScheduler.clear()
        val normalizedOffset = centDiff.coerceIn(-pointerRange, pointerRange) / pointerRange
        val maxOffset = (container.width - pointer.width) / 2f
        pointer.translationX = normalizedOffset * maxOffset
    }

    /**
     * 停止当前调音监听。
     */
    private fun stopTuner() {
        audioDispatcher?.stop()
        audioDispatcher = null
        audioThread?.interrupt()
        audioThread = null
        smoothedPitch = null
    }

    /**
     * 播放当前选中琴弦的参考音。
     */
    private fun playSelectedStringTone() {
        tonePreviewPlayer.play(getSelectedString().frequency)
    }

    /**
     * 在视图销毁时释放首页相关资源。
     */
    override fun onDestroyView() {
        pitchPointerUpdateScheduler.clear()
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
         * 播放指定频率的短促参考音。
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
                                sampleIndex < fadeSamples -> sampleIndex.toDouble() / fadeSamples
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
         * 停止当前正在播放的参考音。
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
         * 安全释放 AudioTrack 资源。
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

internal class PitchPointerUpdateScheduler(
    private val onUpdate: (Float) -> Unit
) {
    private var pendingUpdate: PendingUpdate? = null

    fun schedule(
        centDiff: Float,
        post: (Runnable) -> Unit,
        remove: (Runnable) -> Unit
    ) {
        clear()

        var scheduledUpdate: PendingUpdate? = null
        val runnable = Runnable {
            val currentUpdate = pendingUpdate ?: return@Runnable
            if (currentUpdate !== scheduledUpdate) {
                return@Runnable
            }
            pendingUpdate = null
            onUpdate(currentUpdate.centDiff)
        }

        scheduledUpdate = PendingUpdate(
            centDiff = centDiff,
            runnable = runnable,
            remove = remove
        )
        pendingUpdate = scheduledUpdate
        post(runnable)
    }

    fun clear() {
        pendingUpdate?.let { update ->
            update.remove(update.runnable)
        }
        pendingUpdate = null
    }

    private class PendingUpdate(
        val centDiff: Float,
        val runnable: Runnable,
        val remove: (Runnable) -> Unit
    )
}
