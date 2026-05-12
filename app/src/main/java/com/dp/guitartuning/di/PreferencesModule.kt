package com.dp.guitartuning.di

import android.content.Context
import com.dp.guitartuning.common.data.preferences.PreferenceManager
import com.dp.guitartuning.common.data.preferences.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class PreferencesModule {

    /**
     * 提供 preferences。
     */
    @Provides
    @Singleton
    fun providePreferences(@ApplicationContext context: Context): Preferences {
        return PreferenceManager(context)
    }
}
