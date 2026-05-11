package com.dp.truning.ui.activitys

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.dp.truning.R
import android.os.ParcelFileDescriptor
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySettingsNavigationTest {

    /**
     * 验证 settings tab opens home then placeholder then back to home。
     */
    @Test
    fun settingsTab_opensHome_thenPlaceholder_thenBackToHome() {
        launchMainActivity()

        onView(withId(R.id.bottomNav)).perform(selectBottomNavItem(R.id.tab_mine))
        onView(withId(R.id.settingsHomeTitle)).check(matches(withText(R.string.settings_root_title)))
        onView(withId(R.id.itemTuner)).check(matches(isDisplayed()))
        onView(withId(R.id.itemAbout)).check(matches(isDisplayed()))

        onView(withId(R.id.itemTuner)).perform(click())
        onView(withText(R.string.settings_title)).check(matches(isDisplayed()))
        onView(withText(R.string.settings_reference_a4_title)).check(matches(isDisplayed()))
        onView(withId(R.id.buttonBack)).perform(click())
        onView(withId(R.id.settingsHomeTitle)).check(matches(withText(R.string.settings_root_title)))

        onView(withId(R.id.itemAbout)).perform(click())
        onView(withId(R.id.titleView)).check(matches(withText(R.string.settings_item_about)))
        onView(withId(R.id.messageView)).check(matches(withText(R.string.settings_placeholder_message)))

        onView(withId(R.id.buttonBack)).perform(click())
        onView(withId(R.id.settingsHomeTitle)).check(matches(withText(R.string.settings_root_title)))
        onView(withId(R.id.itemAbout)).check(matches(isDisplayed()))
    }

    /**
     * 启动 main activity。
     */
    private fun launchMainActivity() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        ParcelFileDescriptor.AutoCloseInputStream(
            instrumentation.uiAutomation.executeShellCommand(
                "am start -W -n com.ferhatozcelik.androidmvvmtemplate/.ui.activitys.MainActivity " +
                    "--ez ${MainActivity.EXTRA_SKIP_HOME_AUTO_ENTRY} true"
            )
        ).use { input ->
            while (input.read() != -1) {
                // Consume shell output so the command can complete cleanly.
            }
        }
        instrumentation.waitForIdleSync()
    }

    /**
     * 选择 bottom nav item。
     */
    private fun selectBottomNavItem(itemId: Int): ViewAction {
        return object : ViewAction {
            /**
             * 获取 constraints。
             */
            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(BottomNavigationView::class.java)
            }

            /**
             * 获取 description。
             */
            override fun getDescription(): String {
                return "select bottom navigation item $itemId"
            }

            /**
             * 处理 perform 相关逻辑。
             */
            override fun perform(uiController: UiController, view: View) {
                val bottomNav = view as BottomNavigationView
                bottomNav.selectedItemId = itemId
                uiController.loopMainThreadUntilIdle()
            }
        }
    }
}
