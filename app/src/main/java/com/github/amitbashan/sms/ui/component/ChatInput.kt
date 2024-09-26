package com.github.amitbashan.sms.ui.component

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
fun ChatInput(message: MutableState<String>, onClick: () -> Unit) {
    Row(modifier = Modifier.height(IntrinsicSize.Min).fillMaxWidth()) {
        TextField(
            modifier = Modifier.weight(0.8f).fillMaxSize(),
            value = message.value,
            shape = RoundedCornerShape(10.dp),
            onValueChange = { message.value = it },
            maxLines = 2,
        )
        OutlinedButton(
            modifier = Modifier.weight(0.2f).fillMaxSize(),
            shape = CircleShape,
            onClick = onClick,
            enabled = message.value.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send message",
                modifier = Modifier.size(25.dp)
            )
        }
    }
}