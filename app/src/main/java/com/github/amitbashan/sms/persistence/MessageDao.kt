package com.github.amitbashan.sms.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM Message WHERE originatingAddress = :originatingAddress ORDER BY timestamp")
    fun getConversationOf(originatingAddress: String): Flow<Conversation>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun pushMessage(message: Message)
}