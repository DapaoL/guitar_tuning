package com.dp.guitartuning.ui.fragments.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.dp.guitartuning.R
import com.dp.guitartuning.databinding.FragmentSupportFeedbackBinding
import com.dp.guitartuning.ui.base.BaseFragment

class SupportFeedbackFragment : BaseFragment<FragmentSupportFeedbackBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            (parentFragment as? SettingsNavigationHost)?.goBackFromSettingsChild()
        }

        binding.itemEmail.setOnClickListener {
            copyEmailToClipboard()
        }

        binding.itemRate.setOnClickListener {
            Toast.makeText(requireContext(), R.string.support_feedback_rate_placeholder, Toast.LENGTH_SHORT).show()
        }

        binding.itemShare.setOnClickListener {
            Toast.makeText(requireContext(), R.string.support_feedback_share_placeholder, Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyEmailToClipboard() {
        try {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("email", getString(R.string.support_feedback_email_value))
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), R.string.support_feedback_copy_success, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.support_feedback_copy_failed, Toast.LENGTH_SHORT).show()
        }
    }
}
