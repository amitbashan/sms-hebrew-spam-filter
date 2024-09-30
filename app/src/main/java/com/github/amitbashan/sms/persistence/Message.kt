package com.github.amitbashan.sms.persistence

import androidx.room.Entity
import androidx.room.ForeignKey

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