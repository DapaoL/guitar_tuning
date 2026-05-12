package com.dp.guitartuning.data.local

import androidx.room.TypeConverter
import java.util.Date

class Converters {

    /**
     * 将输入转换为 timestamp。
     */
    @TypeConverter fun fromTimestamp(value: Long?): Date {
        return value?.let { Date(it) } ?: Date(System.currentTimeMillis())
    }

    /**
     * 将当前值转换为 timestamp。
     */
    @TypeConverter fun toTimestamp(value: Date?): Long {
        return value?.let { value.time } ?: System.currentTimeMillis()
    }
}