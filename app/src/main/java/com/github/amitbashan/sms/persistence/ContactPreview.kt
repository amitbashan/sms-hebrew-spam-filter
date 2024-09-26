package com.github.amitbashan.sms.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContactPreview(
    @PrimaryKey
    val originatingAddress: String,
    val timestamp: Long,
    val content: String?,
)
