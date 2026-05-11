package com.dp.truning.ui.fragments.detail

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
import android.view.animation.LinearInterpolator
import android.widget.Toast
import com.dp.truning.R
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioGenerator
import be.tarsos.dsp.AudioProcessor
import com.dp.truning.databinding.FragmentDetailBinding
import com.dp.truning.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sin

@AndroidEntryPoint
class DetailFragment : BaseFragment<FragmentDetailBinding, DetailViewModel>() {
    private val TAG = DetailFragment::class.java.simpleName

    private val sampleRate = 44100
    private val bufferSize = 1024

    private var generator: AudioGenerator? = null
    private var generatorThread: Thread? = null
    private var metronome: MetronomeProcessor? = null
    private var bpm = 120
    private var ringRotationAnimator: ObjectAnimator? = null
    private var isUserRotatingRing = false
    private val beatHandler = Handler(Looper.getMainLooper())
    private var visualBeatIndex = 0
    private val beatVisualizerRunnable = object : Runnable {
        override fun run() {
            showBeat(visualBeatIndex)
            visualBeatIndex = (visualBeatIndex + 1) % BEATS_PER_BAR
            beatHandler.postDelayed(this, beatIntervalMillis())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.page = this
        binding.vm = viewModel
        binding.lifecycleOwner = this
        updateBpmLabel()
        updateBpmInput()
        updateRingRotationFromBpm()
        setupRingSpeedControl()
        resetBeatVisualizer()
    }

    fun start(@Suppress("UNUSED_PARAMETER") view: View) {
        if (generator != null) return
        if (!syncCustomBpm()) return
        val mp = MetronomeProcessor(sampleRate, bpm)
        val gen = AudioGenerator(bufferSize, 0, sampleRate).apply {
            addAudioProcessor(mp)
            addAudioProcessor(AudioTrackPlayer(sampleRate, bufferSize))
        }
        metronome = mp
        generator = gen
        generatorThread = Thread(gen, "metronome-gen").also { it.start() }
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
        super.onDestroyView()
    }

    private fun syncCustomBpm(): Boolean {
        val customBpm = binding.bpmInput.text.toString().trim().toIntOrNull()
        if (customBpm == null || customBpm !in MIN_BPM..MAX_BPM) {
            binding.bpmInput.error = "请输入 $MIN_BPM 到 $MAX_BPM 之间的速度"
            Toast.makeText(requireContext(), "BPM 无效", Toast.LENGTH_SHORT).show()
            return false
        }

        bpm = customBpm
        applyBpm(bpm, updateInput = false, updateRing = true)
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
        binding.playPauseButton.contentDescription = if (isPlaying) "暂停" else "播放"
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
        updateRing: Boolean
    ) {
        bpm = newBpm.coerceIn(MIN_BPM, MAX_BPM)
        metronome?.bpm = bpm
        updateBpmLabel()
        if (updateInput) updateBpmInput()
        if (updateRing) updateRingRotationFromBpm()
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
        if (isUserRotatingRing) return
        if (!forceStart && ringRotationAnimator == null) return

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
                    applyBpm(bpmFromTouch(event, ring), updateInput = true, updateRing = true)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    applyBpm(bpmFromTouch(event, ring), updateInput = true, updateRing = true)
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
        if (ringRotationAnimator != null || isUserRotatingRing) return
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
        val blocks = beatBlocks()
        blocks.forEach { block ->
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
                index == 0 -> R.drawable.bg_beat_block_accent
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
        private const val MIN_BPM = 30
        private const val MAX_BPM = 300
        private const val BPM_RANGE = MAX_BPM - MIN_BPM
        private const val BEATS_PER_BAR = 4
        private const val BEAT_PULSE_MILLIS = 130L
        private const val FULL_ROTATION_DEGREES = 360f
        private const val QUARTER_ROTATION_DEGREES = 90f
        private const val RING_ROTATION_BEATS_PER_CYCLE = 4f
        private const val RING_ROTATION_SPEED_MULTIPLIER = 1f
    }

    private class MetronomeProcessor(
        private val sampleRate: Int,
        initialBpm: Int
    ) : AudioProcessor {
        @Volatile var bpm: Int = initialBpm
        private val clickLenSamples = (sampleRate * 0.03).toInt()
        private val clickFreq = 1000.0
        private var sampleCounter = 0L

        override fun process(audioEvent: AudioEvent): Boolean {
            val buf = audioEvent.floatBuffer
            val intervalSamples = (sampleRate * 60.0 / bpm).toLong().coerceAtLeast(1L)
            for (i in buf.indices) {
                val pos = ((sampleCounter + i) % intervalSamples).toInt()
                buf[i] = if (pos < clickLenSamples) {
                    val t = pos.toDouble() / sampleRate
                    val env = 1.0 - pos.toDouble() / clickLenSamples
                    (sin(2 * PI * clickFreq * t) * env * 0.6).toFloat()
                } else 0f
            }
            sampleCounter += buf.size
            return true
        }

        override fun processingFinished() {}
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
                val s = floats[i].coerceIn(-1f, 1f) * Short.MAX_VALUE
                out[i] = s.toInt().toShort()
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
