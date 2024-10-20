package com.github.amitbashan.sms.ui.component

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.amitbashan.sms.persistence.MessageStatus

@Preview
@Composable
fun Message(
    message: String = "Hello world!",
    isMe: Boolean = false,
    isSpam: Boolean = false,
    probability: Float = 0F,
    messageStatus: MessageStatus? = null
) {
    val context = LocalContext.current

    Row(
        Modifier
            .fillMaxSize()
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isMe) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        if (isMe) {
            when (messageStatus) {
                null -> Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Pending",
                    modifier = Modifier.size(20.dp)
                )

                MessageStatus.Sent -> Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Sent",
                    modifier = Modifier.size(15.dp)
                )

                MessageStatus.Delivered -> Icon(
                    Icons.Default.Check,
                    contentDescription = "Delivered",
                    modifier = Modifier.size(20.dp)
                )
            }
        } else if (isSpam) {
            IconButton(onClick = {
                Toast.makeText(context, "Probability = ${probability * 100}%", Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = "Message detected as spam",
                )
            }
        }
        Box(
            Modifier
                .wrapContentSize()
                .background(
                    if (isMe) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.tertiaryContainer
                    }, RoundedCornerShape(10.dp)
                )
                .padding(5.dp)
        ) {
            Text(
                message, color = if (isMe) {
                    MaterialTheme.colorScheme.onSecondary
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                }
            )
        }
    }
}