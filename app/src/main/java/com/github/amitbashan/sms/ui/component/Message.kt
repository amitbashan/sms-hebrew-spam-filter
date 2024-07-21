package com.github.amitbashan.sms.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Message(message: String, isMe: Boolean = false) {
    Box(Modifier.wrapContentSize().background(if (isMe) { MaterialTheme.colorScheme.primary } else { MaterialTheme.colorScheme.secondary }, RoundedCornerShape(5)).padding(5.dp)) {
        Text(message, color = if (isMe) { MaterialTheme.colorScheme.onPrimary } else { MaterialTheme.colorScheme.onSecondary })
    }
}