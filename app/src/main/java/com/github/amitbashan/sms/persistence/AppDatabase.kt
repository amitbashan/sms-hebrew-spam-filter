package com.github.amitbashan.sms.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Contact::class, ContactPreview::class, Message::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao

    abstract fun contactPreviewDao(): ContactPreviewDao

    abstract fun messageDao(): MessageDao

    companion object {
        private var dbInstance: AppDatabase? = null

        fun initialize(context: Context) {
            if (dbInstance == null) {
                dbInstance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "testdb20102024"
                ).build()
            }
        }

        fun getInstance(): AppDatabase? {
            return dbInstance
        }
    }
}