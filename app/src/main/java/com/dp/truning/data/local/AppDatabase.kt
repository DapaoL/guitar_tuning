package com.dp.truning.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dp.truning.data.dao.ExampleDao
import com.dp.truning.data.entity.ExampleEntity
import com.dp.truning.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [ExampleEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getExampleDao(): ExampleDao

    class Callback @Inject constructor(private val database: Provider<AppDatabase>,
                                       @ApplicationScope private val applicationScope: CoroutineScope) : RoomDatabase.Callback()
}