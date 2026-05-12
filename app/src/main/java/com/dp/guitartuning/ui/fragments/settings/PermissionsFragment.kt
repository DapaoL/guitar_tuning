package com.dp.guitartuning.ui.fragments.settings

import android.os.Bundle
import android.view.View
import com.dp.guitartuning.databinding.FragmentPermissionsBinding
import com.dp.guitartuning.ui.base.BaseFragment

class PermissionsFragment : BaseFragment<FragmentPermissionsBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            (parentFragment as? SettingsNavigationHost)?.goBackFromSettingsChild()
        }
    }
}
