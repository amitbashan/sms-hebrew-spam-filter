package com.github.amitbashan.sms.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MessageDao {
    @Query("SELECT * FROM Message WHERE originatingAddress = :originatingAddress ORDER BY timestamp")
    fun getConversationOf(originatingAddress: String): Flow<Conversation>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun pushMessage(message: Message)

    @Query("UPDATE Message SET messageStatus = :status WHERE Message.originatingAddress = :originatingAddress AND Message.timestamp = :timestamp")
    fun updateMessageStatus(originatingAddress: String, timestamp: Long, status: MessageStatus)
}