package com.github.amitbashan.sms.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Message::class,
            parentColumns = ["originatingAddress", "timestamp"],
            childColumns = ["originatingAddress", "timestamp"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ]
)
data class ContactPreview(
    @PrimaryKey
    val originatingAddress: String,
    val timestamp: Long,
    @ColumnInfo
    val content: String,
)
