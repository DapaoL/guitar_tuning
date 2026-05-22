package com.dp.guitartuning.ui.fragments.detail

import androidx.lifecycle.ViewModel
import com.dp.guitartuning.common.data.preferences.Preferences
import com.dp.guitartuning.domain.model.MetronomeSettings
import com.dp.guitartuning.domain.model.MetronomeTimeSignature
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val preferences: Preferences
) : ViewModel() {

    fun getMetronomeSettings(): MetronomeSettings {
        return preferences.getMetronomeSettings()
    }

    fun setMetronomeLastBpm(lastBpm: Int) {
        preferences.setMetronomeLastBpm(lastBpm)
    }

    fun setMetronomeTimeSignature(timeSignature: MetronomeTimeSignature) {
        preferences.setMetronomeTimeSignature(timeSignature)
    }

    fun getKeepScreenOnEnabled(): Boolean {
        return preferences.getGeneralSettings().keepScreenOnEnabled
    }

    fun getVolumeBoostEnabled(): Boolean {
        return preferences.getGeneralSettings().volumeBoostEnabled
    }
}
