package com.dp.truning.di

import android.app.Application
import androidx.room.Room
import com.dp.truning.data.dao.ExampleDao
import com.dp.truning.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * 提供 database。
     */
    @Provides
    @Singleton
    fun provideDatabase(application: Application, callback: AppDatabase.Callback): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java, "local_database").fallbackToDestructiveMigration().addCallback(callback).build()
    }

    /**
     * 提供 example dao。
     */
    @Provides
    fun provideExampleDao(database: AppDatabase): ExampleDao {
        return database.getExampleDao()
    }
}