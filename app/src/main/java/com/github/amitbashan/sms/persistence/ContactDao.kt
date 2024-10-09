package com.github.amitbashan.sms.persistence

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM Contact")
    fun getAll(): Flow<List<Contact>>

    @Upsert
    suspend fun upsert(contact: Contact)

    @Query("SELECT EXISTS (SELECT * FROM Contact WHERE Contact.originatingAddress = :originatingAddress)")
    suspend fun exists(originatingAddress: String): Boolean

    @Query("SELECT EXISTS (SELECT * FROM Contact WHERE Contact.originatingAddress = :originatingAddress AND Contact.isSpammer = TRUE)")
    suspend fun isSpammer(originatingAddress: String): Boolean

    @Query("UPDATE Contact SET isSpammer = :isSpammer WHERE originatingAddress = :originatingAddress")
    suspend fun setSpamStatus(originatingAddress: String, isSpammer: Boolean)

    suspend fun insertIfDoesntExist(originatingAddress: String, isSpammer: Boolean) {
        if (!exists(originatingAddress)) {
            upsert(Contact(originatingAddress, isSpammer))
        }
    }
}