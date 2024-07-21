package com.github.amitbashan.sms.persistence

import androidx.room.TypeConverter
import java.time.LocalDateTime

class DateTypeConverters {
    @TypeConverter
    fun fromTimestamp(value: String): LocalDateTime {
        return LocalDateTime.parse(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime): String {
        return date.toString()
    }
}