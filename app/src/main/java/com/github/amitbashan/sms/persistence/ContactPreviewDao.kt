package com.github.amitbashan.sms.persistence

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ContactPreviewDao {
    @Query("SELECT * FROM ContactPreview")
    fun getAll(): Flow<List<ContactPreview>>

    @Upsert
    fun upsert(preview: ContactPreview)
}