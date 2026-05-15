package com.dp.guitartuning.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import com.dp.guitartuning.R
import com.dp.guitartuning.databinding.FragmentSettingsBinding
import com.dp.guitartuning.domain.model.TunerDisplayMode
import com.dp.guitartuning.domain.model.TuningSensitivity
import com.dp.guitartuning.ui.base.BaseVmFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseVmFragment<FragmentSettingsBinding, SettingsViewModel>() {
    private var isUpdatingSelectionFromState = false

    /**
     * 在视图创建完成后绑定界面状态与交互。
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        bindObservers()
    }

    /**
     * 在界面恢复可见时刷新当前设置。
     */
    override fun onResume() {
        super.onResume()
        viewModel.loadSettings()
    }

    /**
     * 初始化页面交互。
     */
    private fun initView() {
        binding.page = this

        binding.etReferenceA4.doAfterTextChanged { text ->
            val value = text?.toString().orEmpty()
            if (viewModel.referenceA4Input.value != value) {
                viewModel.onReferenceA4Changed(value)
            }
        }
    }

    /**
     * 绑定 ViewModel 观察者。
     */
    fun goBack(@Suppress("UNUSED_PARAMETER") view: View) {
        (parentFragment as? SettingsNavigationHost)?.goBackFromSettingsChild()
    }

    fun applyReferenceA4(@Suppress("UNUSED_PARAMETER") view: View) {
        viewModel.saveReferenceA4()
    }

    fun selectHighSensitivity(@Suppress("UNUSED_PARAMETER") view: View) {
        selectSensitivity(TuningSensitivity.HIGH)
    }

    fun selectMediumSensitivity(@Suppress("UNUSED_PARAMETER") view: View) {
        selectSensitivity(TuningSensitivity.MEDIUM)
    }

    fun selectLowSensitivity(@Suppress("UNUSED_PARAMETER") view: View) {
        selectSensitivity(TuningSensitivity.LOW)
    }

    fun selectGaugeDisplay(@Suppress("UNUSED_PARAMETER") view: View) {
        selectDisplayMode(TunerDisplayMode.GAUGE)
    }

    fun selectPointerDisplay(@Suppress("UNUSED_PARAMETER") view: View) {
        selectDisplayMode(TunerDisplayMode.POINTER)
    }

    fun selectNumericDisplay(@Suppress("UNUSED_PARAMETER") view: View) {
        selectDisplayMode(TunerDisplayMode.NUMERIC)
    }

    private fun selectSensitivity(sensitivity: TuningSensitivity) {
        if (isUpdatingSelectionFromState) {
            return
        }
        viewModel.selectSensitivity(sensitivity)
    }

    private fun selectDisplayMode(displayMode: TunerDisplayMode) {
        if (isUpdatingSelectionFromState) {
            return
        }
        viewModel.selectDisplayMode(displayMode)
    }

    private fun bindObservers() {
        viewModel.referenceA4Input.observe(viewLifecycleOwner) { input ->
            val current = binding.etReferenceA4.text?.toString().orEmpty()
            if (current != input) {
                binding.etReferenceA4.setText(input)
                binding.etReferenceA4.setSelection(input.length)
            }
        }

        viewModel.referenceA4HasError.observe(viewLifecycleOwner) { hasError ->
            binding.referenceA4InputLayout.error =
                if (hasError) getString(R.string.settings_reference_a4_error_range) else null
        }

        viewModel.selectedSensitivity.observe(viewLifecycleOwner) { sensitivity ->
            val targetId = when (sensitivity) {
                TuningSensitivity.HIGH -> R.id.optionSensitivityHigh
                TuningSensitivity.MEDIUM -> R.id.optionSensitivityMedium
                TuningSensitivity.LOW -> R.id.optionSensitivityLow
            }
            if (binding.groupSensitivity.checkedRadioButtonId != targetId) {
                isUpdatingSelectionFromState = true
                binding.groupSensitivity.check(targetId)
                isUpdatingSelectionFromState = false
            }
        }

        viewModel.selectedDisplayMode.observe(viewLifecycleOwner) { displayMode ->
            val targetId = when (displayMode) {
                TunerDisplayMode.GAUGE -> R.id.optionDisplayGauge
                TunerDisplayMode.POINTER -> R.id.optionDisplayPointer
                TunerDisplayMode.NUMERIC -> R.id.optionDisplayNumeric
            }
            if (binding.groupDisplayMode.checkedRadioButtonId != targetId) {
                isUpdatingSelectionFromState = true
                binding.groupDisplayMode.check(targetId)
                isUpdatingSelectionFromState = false
            }
        }
    }
}
