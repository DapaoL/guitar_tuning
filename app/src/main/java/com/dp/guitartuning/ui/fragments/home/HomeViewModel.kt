package com.dp.guitartuning.ui.fragments.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dp.guitartuning.common.data.preferences.Preferences
import com.dp.guitartuning.domain.model.InstrumentConfig
import com.dp.guitartuning.domain.model.InstrumentType
import com.dp.guitartuning.domain.model.TunerSettings
import com.dp.guitartuning.domain.model.TuningPreset
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferences: Preferences
) : ViewModel() {

    var name = MutableLiveData("5")
    var selectedLabel = MutableLiveData("A")
    var selectedIndex = MutableLiveData(4)
    var autoDetectEnabled = MutableLiveData(true)
    val tunerSettings = MutableLiveData(TunerSettings())

    /**
     * 当前选中的乐器配置，默认为吉他。
     */
    val currentInstrument = MutableLiveData(InstrumentConfig.GUITAR)

    /**
     * 当前选中的调弦预设，默认为标准调弦。
     */
    val currentTuningPreset = MutableLiveData(InstrumentConfig.GUITAR.standardTuning)

    /**
     * 刷新调音器设置，确保首页展示与最新配置保持一致。
     */
    fun refreshTunerSettings() {
        tunerSettings.value = preferences.getTunerSettings()
    }

    fun getKeepScreenOnEnabled(): Boolean {
        return preferences.getGeneralSettings().keepScreenOnEnabled
    }

    /**
     * 切换当前乐器，并同步重置为标准调弦和该乐器的默认弦。
     * 如果传入的类型与当前乐器相同，则不做任何更改。
     */
    fun switchInstrument(type: InstrumentType) {
        val newConfig = InstrumentConfig.forType(type)
        if (newConfig.instrumentType == currentInstrument.value?.instrumentType) {
            return
        }
        val preset = newConfig.standardTuning
        currentInstrument.value = newConfig
        currentTuningPreset.value = preset
        resetToDefaultString(preset)
    }

    /**
     * 重置选中弦到当前调弦预设的默认弦。
     */
    private fun resetToDefaultString(preset: TuningPreset) {
        val defaultIndex = preset.defaultStringIndex.coerceIn(0, preset.strings.lastIndex)
        val defaultString = preset.strings.getOrNull(defaultIndex) ?: preset.strings.firstOrNull() ?: return
        selectedIndex.value = defaultString.index
        name.value = defaultString.number
        selectedLabel.value = defaultString.label
    }

    /**
     * 返回当前乐器当前调弦预设的弦列表；若为空则回退到吉他标准弦。
     */
    fun currentStrings(referenceA4Hz: Int): List<com.dp.guitartuning.domain.model.InstrumentString> {
        val preset = currentTuningPreset.value ?: InstrumentConfig.GUITAR.standardTuning
        return preset.strings.takeIf { it.isNotEmpty() }
            ?: InstrumentConfig.GUITAR.standardTuning.strings
    }

    /**
     * 返回当前选中弦（按 referenceA4Hz 换算频率）；若越界则返回默认弦。
     */
    fun currentSelectedString(referenceA4Hz: Int): com.dp.guitartuning.domain.model.InstrumentString {
        val strings = currentStrings(referenceA4Hz)
        val index = selectedIndex.value ?: 0
        return strings.getOrNull(index)
            ?: strings.getOrNull(currentTuningPreset.value?.defaultStringIndex ?: 0)
            ?: strings.first()
    }
}
