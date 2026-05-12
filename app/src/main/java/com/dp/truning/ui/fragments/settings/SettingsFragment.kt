package com.dp.truning.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import com.dp.truning.R
import com.dp.truning.databinding.FragmentSettingsBinding
import com.dp.truning.domain.model.TunerDisplayMode
import com.dp.truning.domain.model.TuningSensitivity
import com.dp.truning.ui.base.BaseVmFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseVmFragment<FragmentSettingsBinding, SettingsViewModel>() {
    private var isUpdatingSelectionFromState = false

    /**
     * 在视图创建完成后绑定界面状态与交互。
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
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
        binding.buttonBack.setOnClickListener {
            (parentFragment as? SettingsNavigationHost)?.goBackFromSettingsChild()
        }

        binding.etReferenceA4.doAfterTextChanged { text ->
            val value = text?.toString().orEmpty()
            if (viewModel.referenceA4Input.value != value) {
                viewModel.onReferenceA4Changed(value)
            }
        }

        binding.btnApplyA4.setOnClickListener {
            viewModel.saveReferenceA4()
        }

        binding.groupSensitivity.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingSelectionFromState) {
                return@setOnCheckedChangeListener
            }
            when (checkedId) {
                R.id.optionSensitivityHigh -> viewModel.selectSensitivity(TuningSensitivity.HIGH)
                R.id.optionSensitivityMedium -> viewModel.selectSensitivity(TuningSensitivity.MEDIUM)
                R.id.optionSensitivityLow -> viewModel.selectSensitivity(TuningSensitivity.LOW)
            }
        }

        binding.groupDisplayMode.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingSelectionFromState) {
                return@setOnCheckedChangeListener
            }

            when (checkedId) {
                R.id.optionDisplayGauge -> viewModel.selectDisplayMode(TunerDisplayMode.GAUGE)
                R.id.optionDisplayPointer -> viewModel.selectDisplayMode(TunerDisplayMode.POINTER)
                R.id.optionDisplayNumeric -> viewModel.selectDisplayMode(TunerDisplayMode.NUMERIC)
            }
        }
    }

    /**
     * 绑定 ViewModel 观察者。
     */
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
