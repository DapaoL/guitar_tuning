package com.dp.truning.ui.activitys

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.dp.truning.R
import com.dp.truning.databinding.ActivityMainBinding
import com.dp.truning.ui.base.BaseActivity
import com.dp.truning.ui.fragments.detail.DetailFragment
import com.dp.truning.ui.fragments.home.HomeFragment
import com.dp.truning.ui.fragments.settings.SettingsContainerFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private val TAG = MainActivity::class.java.simpleName
    private var currentPrimaryTabId: Int = R.id.tab_home
    private var previousPrimaryTabId: Int = R.id.tab_home
    private var navigationBarInsetBottom: Int = 0
    private var bottomSafeArea: Int = 0

    private val safeAreaLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            fragment: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            applyBottomSafeArea(v)
        }
    }

    companion object {
        const val EXTRA_SKIP_HOME_AUTO_ENTRY = "skip_home_auto_entry"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
        supportFragmentManager.registerFragmentLifecycleCallbacks(safeAreaLifecycleCallbacks, false)
        setupBottomSafeAreaHandling()

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
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(safeAreaLifecycleCallbacks)
        super.onDestroy()
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

    private fun setupBottomSafeAreaHandling() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            navigationBarInsetBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            updateBottomSafeArea()
            insets
        }

        binding.bottomNav.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateBottomSafeArea()
        }

        binding.bottomNav.post {
            updateBottomSafeArea()
            ViewCompat.requestApplyInsets(binding.root)
        }
    }

    private fun updateBottomSafeArea() {
        val layoutParams = binding.bottomNav.layoutParams as? android.widget.FrameLayout.LayoutParams ?: return
        val safeArea = binding.bottomNav.height + layoutParams.bottomMargin + navigationBarInsetBottom

        if (safeArea == bottomSafeArea) {
            return
        }

        bottomSafeArea = safeArea
        supportFragmentManager.fragments.forEach { fragment ->
            fragment.view?.let(::applyBottomSafeArea)
        }
    }

    private fun applyBottomSafeArea(view: View) {
        val originalBottomPadding = (view.getTag(R.id.tag_original_bottom_padding) as? Int)
            ?: view.paddingBottom.also { view.setTag(R.id.tag_original_bottom_padding, it) }

        view.updatePadding(bottom = originalBottomPadding + bottomSafeArea)
    }

}
