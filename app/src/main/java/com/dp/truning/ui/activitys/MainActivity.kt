package com.dp.truning.ui.activitys

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.dp.truning.R
import com.dp.truning.databinding.ActivityMainBinding
import com.dp.truning.ui.base.BaseActivity
import com.dp.truning.ui.fragments.detail.DetailFragment
import com.dp.truning.ui.fragments.home.HomeFragment
import com.dp.truning.ui.fragments.settings.GeneralSettingsViewModel
import com.dp.truning.ui.fragments.settings.SettingsContainerFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private val tag = MainActivity::class.java.simpleName
    private var currentPrimaryTabId: Int = R.id.tab_home
    private var previousPrimaryTabId: Int = R.id.tab_home
    private var navigationBarInsetBottom: Int = 0
    private var statusBarInsetTop: Int = 0

    private val systemBarLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            fragment: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            applyTopInset(v)
        }
    }

    companion object {
        const val EXTRA_SKIP_HOME_AUTO_ENTRY = "skip_home_auto_entry"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        applyStoredThemeEarly()
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        Log.i(tag, "主页面已创建")
        supportFragmentManager.registerFragmentLifecycleCallbacks(systemBarLifecycleCallbacks, true)
        setupWindowInsetHandling()

        if (savedInstanceState == null) {
            switchPrimaryTab(R.id.tab_home)
        }

        binding.apply {
            bottomNav.itemRippleColor = null
            bottomNav.setOnItemSelectedListener {
                switchPrimaryTab(it.itemId)
                true
            }
            if (savedInstanceState == null) {
                bottomNav.selectedItemId = R.id.tab_home
            }
        }
    }

    override fun onDestroy() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(systemBarLifecycleCallbacks)
        super.onDestroy()
    }

    /**
     * 在 super.onCreate 之前读取主题，避免界面闪烁。
     * 此时 Hilt 注入尚未完成，直接用原始 SharedPreferences 读取。
     */
    private fun applyStoredThemeEarly() {
        val prefs = applicationContext.getSharedPreferences("WiseriaPrefs", android.content.Context.MODE_PRIVATE)
        val raw = prefs.getString("generalThemeMode", null)
        val themeMode = com.dp.truning.domain.model.AppThemeMode.fromStorage(raw)
        GeneralSettingsViewModel.applyTheme(themeMode)
    }

    private fun switch1(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    fun returnToPreviousPrimaryTab() {
        val targetTabId = if (previousPrimaryTabId == R.id.tab_mine) {
            R.id.tab_home
        } else {
            previousPrimaryTabId
        }
        binding.bottomNav.selectedItemId = targetTabId
    }

    private fun switchPrimaryTab(tabId: Int) {
        if (currentPrimaryTabId != tabId) {
            previousPrimaryTabId = currentPrimaryTabId
            currentPrimaryTabId = tabId
        }

        when (tabId) {
            R.id.tab_home -> switch1(HomeFragment())
            R.id.tab_msg -> switch1(DetailFragment())
            R.id.tab_mine -> switch1(SettingsContainerFragment())
        }
    }

    private fun switch(target: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        listOf(HomeFragment(), DetailFragment()).forEach {
            if (it.isAdded) transaction.hide(it)
        }

        if (target.isAdded) {
            transaction.show(target)
        } else {
            transaction.add(R.id.nav_host_fragment, target)
        }

        transaction.commit()
    }

    private fun setupWindowInsetHandling() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            statusBarInsetTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            navigationBarInsetBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            updateBottomNavigationInset()
            applyTopInsetToFragments()
            insets
        }

        binding.root.post {
            ViewCompat.requestApplyInsets(binding.root)
        }
    }

    private fun updateBottomNavigationInset() {
        val originalBottomMargin = (binding.bottomNav.getTag(R.id.tag_original_bottom_margin) as? Int)
            ?: ((binding.bottomNav.layoutParams as? FrameLayout.LayoutParams)?.bottomMargin?.also {
                binding.bottomNav.setTag(R.id.tag_original_bottom_margin, it)
            } ?: return)

        binding.bottomNav.updateLayoutParams<FrameLayout.LayoutParams> {
            bottomMargin = originalBottomMargin + navigationBarInsetBottom
        }
    }

    private fun applyTopInset(view: View) {
        val originalTopPadding = (view.getTag(R.id.tag_original_top_padding) as? Int)
            ?: view.paddingTop.also { view.setTag(R.id.tag_original_top_padding, it) }

        view.updatePadding(top = originalTopPadding + statusBarInsetTop)
    }

    private fun applyTopInsetToFragments(fragmentManager: FragmentManager = supportFragmentManager) {
        fragmentManager.fragments.forEach { fragment ->
            fragment.view?.let(::applyTopInset)
            applyTopInsetToFragments(fragment.childFragmentManager)
        }
    }
}
