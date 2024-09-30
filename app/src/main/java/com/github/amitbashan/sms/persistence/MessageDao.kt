package com.github.amitbashan.sms.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM Message WHERE originatingAddress = :originatingAddress ORDER BY timestamp")
    fun getConversationOf(originatingAddress: String): Flow<Conversation>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun pushMessage(message: Message)

    @Query("UPDATE Message SET messageStatus = :status WHERE Message.originatingAddress = :originatingAddress AND Message.timestamp = :timestamp")
    suspend fun updateMessageStatus(
        originatingAddress: String,
        timestamp: Long,
        status: MessageStatus
    )
}