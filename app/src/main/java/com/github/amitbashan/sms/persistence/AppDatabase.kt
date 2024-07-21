package com.github.amitbashan.sms.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Contact::class, ContactPreview::class, Message::class],
    version = 1
)
@TypeConverters(DateTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao

    abstract fun contactPreviewDao(): ContactPreviewDao

    abstract fun messageDao(): MessageDao

    companion object {
        private lateinit var instance: AppDatabase

        fun initialize(context: Context) {
            instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "sms_db_pr3"
            ).build()
        }

        fun getInstance(): AppDatabase {
            return instance
        }
    }
}