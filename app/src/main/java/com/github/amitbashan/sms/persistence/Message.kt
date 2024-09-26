package com.github.amitbashan.sms.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import java.time.LocalDateTime

@Entity(
    primaryKeys = ["originatingAddress", "timestamp"],
    foreignKeys = [ForeignKey(
        entity = Contact::class,
        parentColumns = ["originatingAddress"],
        childColumns = ["originatingAddress"],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE,
    )]
)
data class Message(
    val originatingAddress: String,
    val timestamp: Long,
    val content: String,
    val isMe: Boolean,
    val messageStatus: MessageStatus?,
)