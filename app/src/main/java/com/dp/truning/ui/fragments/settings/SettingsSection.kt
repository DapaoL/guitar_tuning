package com.dp.truning.ui.fragments.settings

import androidx.annotation.StringRes
import com.dp.truning.R

enum class SettingsSection(
    @StringRes val titleRes: Int,
    val usesPlaceholder: Boolean
) {
    TUNER(R.string.settings_item_tuner, false),
    METRONOME(R.string.settings_item_metronome, false),
    GENERAL(R.string.settings_item_general, true),
    SUPPORT(R.string.settings_item_support, true),
    ABOUT(R.string.settings_item_about, true);

    companion object {
        /**
         * 返回顶层设置分区，顺序与设计稿保持一致。
         */
        fun topLevel(): List<SettingsSection> = listOf(
            TUNER,
            METRONOME,
            GENERAL,
            SUPPORT,
            ABOUT
        )
    }
}
