package com.dp.truning.ui.fragments.detail

import androidx.lifecycle.ViewModel
import com.dp.truning.common.data.preferences.Preferences
import com.dp.truning.domain.model.MetronomeSettings
import com.dp.truning.domain.repository.ExampleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val preferences: Preferences,
    private val exampleRepository: ExampleRepository
) : ViewModel() {

    fun getMetronomeSettings(): MetronomeSettings {
        return preferences.getMetronomeSettings()
    }

    fun setMetronomeLastBpm(lastBpm: Int) {
        preferences.setMetronomeLastBpm(lastBpm)
    }

    fun getKeepScreenOnEnabled(): Boolean {
        return preferences.getGeneralSettings().keepScreenOnEnabled
    }

    fun getVolumeBoostEnabled(): Boolean {
        return preferences.getGeneralSettings().volumeBoostEnabled
    }
}
