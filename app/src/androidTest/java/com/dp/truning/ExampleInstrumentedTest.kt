package com.dp.guitartuning

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
    /**
     * 处理 use app context 相关逻辑。
     */
@RunWith(AndroidJUnit4::class) class ExampleInstrumentedTest {
    @Test fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.ferhatozcelik.androidmvvmtemplate", appContext.packageName)
    }
}