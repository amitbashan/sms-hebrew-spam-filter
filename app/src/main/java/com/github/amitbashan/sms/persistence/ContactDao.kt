package com.github.amitbashan.sms.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM Contact")
    fun getAll(): Flow<List<Contact>>

    @Upsert
    suspend fun upsert(contact: Contact)
}