package com.github.amitbashan.sms.persistence

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactPreviewDao {
    @Query("SELECT * FROM ContactPreview JOIN Contact ON ContactPreview.originatingAddress = Contact.originatingAddress WHERE Contact.isSpammer = :spammers ORDER BY timestamp DESC")
    fun getAll(spammers: Boolean): Flow<List<ContactPreview>>

    @Upsert
    suspend fun upsert(preview: ContactPreview)

    @Query("SELECT * FROM ContactPreview WHERE originatingAddress LIKE :like ORDER BY timestamp DESC")
    fun searchLike(like: String): Flow<List<ContactPreview>>
}