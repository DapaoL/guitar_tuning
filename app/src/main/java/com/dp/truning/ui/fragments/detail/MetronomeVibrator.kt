package com.dp.truning.ui.fragments.detail

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class MetronomeVibrator(context: Context) {

    private val vibrator: Vibrator? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        }

        else -> {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun vibrateAccent() {
        vibrate(durationMillis = 36L, amplitude = 180)
    }

    fun vibrateRegular() {
        vibrate(durationMillis = 20L, amplitude = 110)
    }

    private fun vibrate(durationMillis: Long, amplitude: Int) {
        val targetVibrator = vibrator ?: return
        if (!targetVibrator.hasVibrator()) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            targetVibrator.vibrate(
                VibrationEffect.createOneShot(durationMillis, amplitude)
            )
        } else {
            @Suppress("DEPRECATION")
            targetVibrator.vibrate(durationMillis)
        }
    }
}
