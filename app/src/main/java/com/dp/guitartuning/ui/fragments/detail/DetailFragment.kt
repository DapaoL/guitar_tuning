package com.dp.guitartuning.ui.fragments.detail

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.Toast
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioGenerator
import be.tarsos.dsp.AudioProcessor
import com.dp.guitartuning.R
import com.dp.guitartuning.databinding.FragmentDetailBinding
import com.dp.guitartuning.domain.model.MetronomeBeatType
import com.dp.guitartuning.domain.model.MetronomePlaybackConfig
import com.dp.guitartuning.domain.model.MetronomeSettings
import com.dp.guitartuning.domain.model.MetronomeSoundType
import com.dp.guitartuning.ui.base.BaseVmFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sin

@AndroidEntryPoint
class DetailFragment : BaseVmFragment<FragmentDetailBinding, DetailViewModel>() {

    private val sampleRate = 44100
    private val bufferSize = 1024

    private var generator: AudioGenerator? = null
    private var generatorThread: Thread? = null
    private var metronome: MetronomeProcessor? = null
    private var metronomeVibrator: MetronomeVibrator? = null
    private var playbackConfig = MetronomePlaybackConfig()
    private var bpm = MetronomeSettings.DEFAULT_BPM
    private var ringRotationAnimator: ObjectAnimator? = null
    private var isUserRotatingRing = false
    private val beatHandler = Handler(Looper.getMainLooper())
    private var visualBeatIndex = 0
    private val beatVisualizerRunnable = object : Runnable {
        override fun run() {
            showBeat(visualBeatIndex)
            triggerBeatVibration(visualBeatIndex)
            visualBeatIndex = (visualBeatIndex + 1) % BEATS_PER_BAR
            beatHandler.postDelayed(this, beatIntervalMillis())
        }
    }

    private var volumeBoostEnabled = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.page = this
        binding.vm = viewModel
        binding.lifecycleOwner = this
        metronomeVibrator = MetronomeVibrator(requireContext())
        setupRingSpeedControl()
        refreshMetronomeSettings(applyStoredBpm = true)
        resetBeatVisualizer()
    }

    override fun onResume() {
        super.onResume()
        refreshMetronomeSettings(applyStoredBpm = generator == null)
        applyKeepScreenOn()
    }

    override fun onPause() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
    }

    fun start(@Suppress("UNUSED_PARAMETER") view: View) {
        if (generator != null) {
            return
        }
        if (!syncCustomBpm()) {
            return
        }

        val processor = MetronomeProcessor(sampleRate, playbackConfig, volumeBoostEnabled)
        val audioGenerator = AudioGenerator(bufferSize, 0, sampleRate).apply {
            addAudioProcessor(processor)
            addAudioProcessor(AudioTrackPlayer(sampleRate, bufferSize))
        }
        metronome = processor
        generator = audioGenerator
        generatorThread = Thread(audioGenerator, "metronome-gen").also { it.start() }
        startRingRotation()
        startBeatVisualizer()
        updatePlayPauseButton()
    }

    fun stop(@Suppress("UNUSED_PARAMETER") view: View) {
        generator?.stop()
        generator = null
        generatorThread = null
        metronome = null
        stopRingRotation()
        stopBeatVisualizer()
        updatePlayPauseButton()
    }

    fun togglePlayback(view: View) {
        if (generator == null) {
            start(view)
        } else {
            stop(view)
        }
    }

    fun applyCustomSpeed(@Suppress("UNUSED_PARAMETER") view: View) {
        syncCustomBpm()
    }

    override fun onDestroyView() {
        stop(requireView())
        beatHandler.removeCallbacks(beatVisualizerRunnable)
        metronomeVibrator = null
        super.onDestroyView()
    }

    private fun refreshMetronomeSettings(applyStoredBpm: Boolean) {
        val settings = viewModel.getMetronomeSettings()
        volumeBoostEnabled = viewModel.getVolumeBoostEnabled()
        val targetBpm = if (applyStoredBpm) settings.lastBpm else bpm
        playbackConfig = MetronomePlaybackConfig.fromSettings(settings).copy(bpm = targetBpm)
        applyBpm(
            newBpm = targetBpm,
            updateInput = applyStoredBpm,
            updateRing = applyStoredBpm,
            persist = false
        )
    }

    private fun applyKeepScreenOn() {
        val keepOn = viewModel.getKeepScreenOnEnabled()
        if (keepOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun syncCustomBpm(): Boolean {
        val customBpm = binding.bpmInput.text.toString().trim().toIntOrNull()
        if (customBpm == null || customBpm !in MIN_BPM..MAX_BPM) {
            binding.bpmInput.error = getString(R.string.metronome_bpm_error_range, MIN_BPM, MAX_BPM)
            Toast.makeText(requireContext(), R.string.metronome_bpm_invalid, Toast.LENGTH_SHORT).show()
            return false
        }

        binding.bpmInput.error = null
        applyBpm(customBpm, updateInput = false, updateRing = true, persist = true)
        updateRingRotationSpeed()
        return true
    }

    private fun updateBpmLabel() {
        binding.currentBpmText.text = bpm.toString()
    }

    private fun updatePlayPauseButton() {
        val isPlaying = generator != null
        binding.playPauseButton.setImageResource(
            if (isPlaying) R.drawable.ic_metronome_pause else R.drawable.ic_metronome_play
        )
        binding.playPauseButton.contentDescription = getString(
            if (isPlaying) R.string.metronome_pause else R.string.metronome_play
        )
    }

    private fun updateBpmInput() {
        val text = bpm.toString()
        if (binding.bpmInput.text.toString() != text) {
            binding.bpmInput.setText(text)
            binding.bpmInput.setSelection(text.length)
        }
        binding.bpmInput.error = null
    }

    private fun applyBpm(
        newBpm: Int,
        updateInput: Boolean,
        updateRing: Boolean,
        persist: Boolean
    ) {
        bpm = newBpm.coerceIn(MIN_BPM, MAX_BPM)
        playbackConfig = playbackConfig.copy(bpm = bpm)
        if (persist) {
            viewModel.setMetronomeLastBpm(bpm)
        }

        metronome?.updateConfig(playbackConfig)
        updateBpmLabel()
        if (updateInput) {
            updateBpmInput()
        } else {
            binding.bpmInput.error = null
        }
        if (updateRing) {
            updateRingRotationFromBpm()
        }
        if (generator != null) {
            beatHandler.removeCallbacks(beatVisualizerRunnable)
            beatHandler.postDelayed(beatVisualizerRunnable, beatIntervalMillis())
        }
    }

    private fun startRingRotation() {
        binding.metronomeRingMarkerContainer.animate().cancel()
        updateRingRotationSpeed(forceStart = true)
    }

    private fun stopRingRotation() {
        ringRotationAnimator?.cancel()
        ringRotationAnimator = null
        updateRingRotationFromBpm()
    }

    private fun updateRingRotationSpeed(forceStart: Boolean = false) {
        if (isUserRotatingRing) {
            return
        }
        if (!forceStart && ringRotationAnimator == null) {
            return
        }

        val ring = binding.metronomeRingMarkerContainer
        val startRotation = ring.rotation
        ringRotationAnimator?.cancel()
        ringRotationAnimator = ObjectAnimator.ofFloat(
            ring,
            View.ROTATION,
            startRotation,
            startRotation + FULL_ROTATION_DEGREES
        ).apply {
            duration = ringRotationDurationMillis()
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            start()
        }
    }

    private fun setupRingSpeedControl() {
        binding.metronomeRingMarkerContainer.setOnTouchListener { ring, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isUserRotatingRing = true
                    ring.parent.requestDisallowInterceptTouchEvent(true)
                    ringRotationAnimator?.cancel()
                    ringRotationAnimator = null
                    applyBpm(bpmFromTouch(event, ring), updateInput = true, updateRing = true, persist = true)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    applyBpm(bpmFromTouch(event, ring), updateInput = true, updateRing = true, persist = true)
                    true
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    isUserRotatingRing = false
                    ring.parent.requestDisallowInterceptTouchEvent(false)
                    if (generator != null && event.actionMasked == MotionEvent.ACTION_UP) {
                        updateRingRotationSpeed(forceStart = true)
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun bpmFromTouch(event: MotionEvent, ring: View): Int {
        val centerX = ring.width / 2f
        val centerY = ring.height / 2f
        val angleFromTop = Math.toDegrees(
            atan2(
                (event.y - centerY).toDouble(),
                (event.x - centerX).toDouble()
            )
        ).toFloat() + QUARTER_ROTATION_DEGREES
        val normalizedAngle = normalizeDegrees(angleFromTop)
        return bpmFromRingRotation(normalizedAngle)
    }

    private fun updateRingRotationFromBpm() {
        if (ringRotationAnimator != null || isUserRotatingRing) {
            return
        }
        binding.metronomeRingMarkerContainer.rotation = ringRotationFromBpm(bpm)
    }

    private fun startBeatVisualizer() {
        visualBeatIndex = 0
        beatHandler.removeCallbacks(beatVisualizerRunnable)
        beatVisualizerRunnable.run()
    }

    private fun stopBeatVisualizer() {
        beatHandler.removeCallbacks(beatVisualizerRunnable)
        resetBeatVisualizer()
    }

    private fun resetBeatVisualizer() {
        beatBlocks().forEach { block ->
            block.setBackgroundResource(R.drawable.bg_beat_block_inactive)
            block.alpha = 1f
            block.scaleX = 1f
            block.scaleY = 1f
        }
    }

    private fun showBeat(activeIndex: Int) {
        beatBlocks().forEachIndexed { index, block ->
            val background = when {
                index != activeIndex -> R.drawable.bg_beat_block_inactive
                playbackConfig.beatTypeFor(index) == MetronomeBeatType.ACCENT -> R.drawable.bg_beat_block_accent
                else -> R.drawable.bg_beat_block_active
            }
            block.setBackgroundResource(background)
            block.animate().cancel()
            block.alpha = if (index == activeIndex) 1f else 0.55f
            block.scaleX = if (index == activeIndex) 1.08f else 1f
            block.scaleY = if (index == activeIndex) 1.08f else 1f
            block.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(if (index == activeIndex) 1f else 0.55f)
                .setDuration(BEAT_PULSE_MILLIS)
                .start()
        }
    }

    private fun triggerBeatVibration(beatIndex: Int) {
        if (!playbackConfig.shouldVibrate(beatIndex)) {
            return
        }

        when (playbackConfig.beatTypeFor(beatIndex)) {
            MetronomeBeatType.ACCENT -> metronomeVibrator?.vibrateAccent()
            MetronomeBeatType.REGULAR -> metronomeVibrator?.vibrateRegular()
        }
    }

    private fun beatBlocks() = listOf(
        binding.beatBlock1,
        binding.beatBlock2,
        binding.beatBlock3,
        binding.beatBlock4
    )

    private fun beatIntervalMillis(): Long {
        return (60_000L / bpm.coerceIn(MIN_BPM, MAX_BPM)).coerceAtLeast(1L)
    }

    private fun ringRotationDurationMillis(): Long {
        val beatsPerRotation = RING_ROTATION_BEATS_PER_CYCLE / ringRotationSpeedMultiplier()
        return (beatIntervalMillis() * beatsPerRotation).toLong().coerceAtLeast(1L)
    }

    private fun ringRotationSpeedMultiplier(): Float {
        return RING_ROTATION_SPEED_MULTIPLIER.coerceAtLeast(0.1f)
    }

    private fun ringRotationFromBpm(value: Int): Float {
        val normalizedBpm = (value.coerceIn(MIN_BPM, MAX_BPM) - MIN_BPM).toFloat() / BPM_RANGE
        return normalizedBpm * FULL_ROTATION_DEGREES
    }

    private fun bpmFromRingRotation(rotation: Float): Int {
        val normalizedRotation = normalizeDegrees(rotation) / FULL_ROTATION_DEGREES
        return (MIN_BPM + normalizedRotation * BPM_RANGE).roundToInt().coerceIn(MIN_BPM, MAX_BPM)
    }

    private fun normalizeDegrees(degrees: Float): Float {
        return ((degrees % FULL_ROTATION_DEGREES) + FULL_ROTATION_DEGREES) % FULL_ROTATION_DEGREES
    }

    companion object {
        private const val MIN_BPM = MetronomeSettings.MIN_BPM
        private const val MAX_BPM = MetronomeSettings.MAX_BPM
        private const val BPM_RANGE = MAX_BPM - MIN_BPM
        private const val BEATS_PER_BAR = 4
        private const val BEAT_PULSE_MILLIS = 130L
        private const val FULL_ROTATION_DEGREES = 360f
        private const val QUARTER_ROTATION_DEGREES = 90f
        private const val RING_ROTATION_BEATS_PER_CYCLE = 4f
        private const val RING_ROTATION_SPEED_MULTIPLIER = 1f
        private const val VOLUME_BOOST_FACTOR = 1.4
    }

    private class MetronomeProcessor(
        private val sampleRate: Int,
        initialConfig: MetronomePlaybackConfig,
        private val volumeBoost: Boolean = false
    ) : AudioProcessor {
        @Volatile
        private var activeConfig: MetronomePlaybackConfig = initialConfig

        @Volatile
        private var pendingConfig: MetronomePlaybackConfig? = null

        private var beatIndex = 0
        private var sampleInBeat = 0L
        private var beatIntervalSamples = intervalSamples(initialConfig.bpm)
        private var currentBeatSpec = buildBeatSpec(initialConfig, beatIndex, sampleRate)

        fun updateConfig(config: MetronomePlaybackConfig) {
            pendingConfig = config
        }

        override fun process(audioEvent: AudioEvent): Boolean {
            val buffer = audioEvent.floatBuffer
            for (index in buffer.indices) {
                if (sampleInBeat == 0L) {
                    pendingConfig?.let { nextConfig ->
                        activeConfig = nextConfig
                        pendingConfig = null
                        beatIntervalSamples = intervalSamples(activeConfig.bpm)
                    }
                    currentBeatSpec = buildBeatSpec(activeConfig, beatIndex, sampleRate)
                }

                buffer[index] = currentBeatSpec.sampleAt(sampleInBeat.toInt())
                sampleInBeat++

                if (sampleInBeat >= beatIntervalSamples) {
                    sampleInBeat = 0L
                    beatIndex = (beatIndex + 1) % BEATS_PER_BAR
                    beatIntervalSamples = intervalSamples(activeConfig.bpm)
                }
            }
            return true
        }

        override fun processingFinished() = Unit

        private fun intervalSamples(bpm: Int): Long {
            return (sampleRate * 60.0 / bpm.coerceIn(MIN_BPM, MAX_BPM)).toLong().coerceAtLeast(1L)
        }

        private fun buildBeatSpec(
            config: MetronomePlaybackConfig,
            beatIndex: Int,
            sampleRate: Int
        ): BeatSpec {
            val isAccent = config.beatTypeFor(beatIndex) == MetronomeBeatType.ACCENT
            val boostFactor = if (volumeBoost) VOLUME_BOOST_FACTOR else 1.0
            return when (config.soundType) {
                MetronomeSoundType.WOOD_BLOCK -> BeatSpec(
                    waveform = Waveform.WOOD,
                    sampleRate = sampleRate,
                    frequency = if (isAccent) 1320.0 else 980.0,
                    amplitude = (if (isAccent) 0.92 else 0.68) * boostFactor,
                    lengthSamples = (sampleRate * 0.03).toInt()
                )

                MetronomeSoundType.CLICK -> BeatSpec(
                    waveform = Waveform.CLICK,
                    sampleRate = sampleRate,
                    frequency = if (isAccent) 1800.0 else 1450.0,
                    amplitude = (if (isAccent) 0.88 else 0.62) * boostFactor,
                    lengthSamples = (sampleRate * 0.018).toInt()
                )

                MetronomeSoundType.DRUM -> BeatSpec(
                    waveform = Waveform.DRUM,
                    sampleRate = sampleRate,
                    frequency = if (isAccent) 280.0 else 220.0,
                    amplitude = (if (isAccent) 0.95 else 0.72) * boostFactor,
                    lengthSamples = (sampleRate * 0.05).toInt()
                )

                MetronomeSoundType.BEEP -> BeatSpec(
                    waveform = Waveform.SINE,
                    sampleRate = sampleRate,
                    frequency = if (isAccent) 1040.0 else 760.0,
                    amplitude = (if (isAccent) 0.78 else 0.56) * boostFactor,
                    lengthSamples = (sampleRate * 0.04).toInt()
                )
            }
        }

        private enum class Waveform {
            WOOD,
            CLICK,
            DRUM,
            SINE
        }

        private data class BeatSpec(
            val waveform: Waveform,
            val sampleRate: Int,
            val frequency: Double,
            val amplitude: Double,
            val lengthSamples: Int
        ) {
            fun sampleAt(sampleIndex: Int): Float {
                if (sampleIndex >= lengthSamples) {
                    return 0f
                }

                val t = sampleIndex.toDouble() / sampleRate
                val phase = 2 * PI * frequency * t
                val progress = sampleIndex.toDouble() / lengthSamples.coerceAtLeast(1)
                val envelope = when (waveform) {
                    Waveform.WOOD -> (1.0 - progress) * (1.0 - progress)
                    Waveform.CLICK -> 1.0 - progress
                    Waveform.DRUM -> (1.0 - progress) * (1.0 - progress) * (1.0 - progress)
                    Waveform.SINE -> 1.0 - progress
                }

                val rawSample = when (waveform) {
                    Waveform.WOOD -> sin(phase) + 0.35 * sin(phase * 2.3)
                    Waveform.CLICK -> if (sin(phase) >= 0) 1.0 else -1.0
                    Waveform.DRUM -> sin(phase) + 0.18 * sin(phase * 0.5)
                    Waveform.SINE -> sin(phase)
                }

                return (rawSample * envelope * amplitude).toFloat()
            }
        }
    }

    private class AudioTrackPlayer(
        sampleRate: Int,
        bufferSize: Int
    ) : AudioProcessor {
        private val track: AudioTrack
        private val scratch: ShortArray

        init {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(bufferSize * 2)
            @Suppress("DEPRECATION")
            track = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuf,
                AudioTrack.MODE_STREAM
            )
            scratch = ShortArray(bufferSize)
            track.play()
        }

        override fun process(audioEvent: AudioEvent): Boolean {
            val floats = audioEvent.floatBuffer
            val n = floats.size
            val out = if (scratch.size == n) scratch else ShortArray(n)
            for (i in 0 until n) {
                val sample = floats[i].coerceIn(-1f, 1f) * Short.MAX_VALUE
                out[i] = sample.toInt().toShort()
            }
            track.write(out, 0, n)
            return true
        }

        override fun processingFinished() {
            try {
                track.stop()
            } catch (_: IllegalStateException) {
            }
            track.release()
        }
    }
}
