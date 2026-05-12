package com.dp.guitartuning.ui.fragments.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.dp.guitartuning.BuildConfig
import com.dp.guitartuning.R
import com.dp.guitartuning.databinding.FragmentAboutSettingsBinding
import com.dp.guitartuning.ui.base.BaseFragment

class AboutSettingsFragment : BaseFragment<FragmentAboutSettingsBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            (parentFragment as? SettingsNavigationHost)?.goBackFromSettingsChild()
        }

        binding.itemPrivacyPolicy.setOnClickListener {
            openUrl(PRIVACY_POLICY_URL)
        }

        binding.itemUserAgreement.setOnClickListener {
            openUrl(USER_AGREEMENT_URL)
        }

        binding.itemPermissions.setOnClickListener {
            (parentFragment as? SettingsNavigationHost)?.openSection(SettingsSection.PERMISSIONS)
        }

        binding.versionValue.text = getString(
            R.string.about_settings_version_format,
            BuildConfig.VERSION_NAME
        )
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (error: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                R.string.about_settings_open_link_failed,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        const val PRIVACY_POLICY_URL = "https://example.com/privacy-policy"
        const val USER_AGREEMENT_URL = "https://example.com/user-agreement"
    }
}
