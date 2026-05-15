package com.dp.guitartuning.ui.fragments.home

import org.junit.Assert.assertEquals
import org.junit.Test

class PitchPointerUpdateSchedulerTest {

    @Test
    fun clear_removesPendingCallbackBeforeItCanRun() {
        val updates = mutableListOf<Float>()
        val callbacks = RecordingCallbackTarget()
        val scheduler = PitchPointerUpdateScheduler { updates += it }

        scheduler.schedule(
            centDiff = 12f,
            post = callbacks::post,
            remove = callbacks::remove
        )
        scheduler.clear()
        callbacks.runPendingCallbacks()

        assertEquals(emptyList<Float>(), updates)
    }

    @Test
    fun schedule_replacesOlderPendingCallbackWithLatestValue() {
        val updates = mutableListOf<Float>()
        val firstCallbacks = RecordingCallbackTarget()
        val secondCallbacks = RecordingCallbackTarget()
        val scheduler = PitchPointerUpdateScheduler { updates += it }

        scheduler.schedule(
            centDiff = -8f,
            post = firstCallbacks::post,
            remove = firstCallbacks::remove
        )
        scheduler.schedule(
            centDiff = 5f,
            post = secondCallbacks::post,
            remove = secondCallbacks::remove
        )

        firstCallbacks.runPendingCallbacks()
        secondCallbacks.runPendingCallbacks()

        assertEquals(listOf(5f), updates)
    }

    private class RecordingCallbackTarget {
        private val pendingCallbacks = mutableListOf<Runnable>()

        fun post(runnable: Runnable) {
            pendingCallbacks += runnable
        }

        fun remove(runnable: Runnable) {
            pendingCallbacks -= runnable
        }

        fun runPendingCallbacks() {
            pendingCallbacks.toList().forEach { runnable ->
                pendingCallbacks -= runnable
                runnable.run()
            }
        }
    }
}
