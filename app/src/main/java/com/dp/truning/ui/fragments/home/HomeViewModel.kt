package com.dp.truning.ui.fragments.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dp.truning.common.data.preferences.Preferences
import com.dp.truning.domain.model.TunerSettings
import com.dp.truning.domain.repository.ExampleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val exampleRepository: ExampleRepository,
    private val preferences: Preferences
) :
    ViewModel() {
    private val tag = HomeViewModel::class.java.simpleName

    var name = MutableLiveData("5")
    var selectedLabel = MutableLiveData("A")
    var selectedIndex = MutableLiveData(4)
    var autoDetectEnabled = MutableLiveData(true)
    val tunerSettings = MutableLiveData(TunerSettings())

    fun refreshTunerSettings() {
        tunerSettings.value = preferences.getTunerSettings()
    }
}
