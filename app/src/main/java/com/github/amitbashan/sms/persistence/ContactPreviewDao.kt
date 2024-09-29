package com.github.amitbashan.sms.persistence

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ContactPreviewDao {
    @Query("SELECT * FROM ContactPreview JOIN Contact ON ContactPreview.originatingAddress = Contact.originatingAddress WHERE Contact.isSpammer = :spammers")
    fun getAll(spammers: Boolean): Flow<List<ContactPreview>>

    @Upsert
    suspend fun upsert(preview: ContactPreview)

    @Query("SELECT * FROM ContactPreview WHERE originatingAddress LIKE :like")
    fun searchLike(like: String): Flow<List<ContactPreview>>
}