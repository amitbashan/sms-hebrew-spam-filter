package com.github.amitbashan.sms.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Contact(
    @PrimaryKey
    val originatingAddress: String,
    @ColumnInfo
    val isSpammer: Boolean,
)