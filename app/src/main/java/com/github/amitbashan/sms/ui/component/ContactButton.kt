package com.github.amitbashan.sms.ui.component

import android.content.Intent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.amitbashan.sms.activity.ChatActivity
import com.github.amitbashan.sms.persistence.ContactPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

@Composable
fun ContactButton(
    preview: ContactPreview,
    activeLongClick: MutableState<String?>,
    blockOnclickHandler: (String) -> Unit = {},
    isSpamActivity: Boolean,
) {
    val context = LocalContext.current
    val contactName = preview.originatingAddress
    val lastMessage = preview.content!!
    val timeOfLastMessage = Instant.ofEpochMilli(preview.timestamp).atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val interactionSource = remember { MutableInteractionSource() }
    val viewConfiguration = LocalViewConfiguration.current
    val isLongClick = activeLongClick.value == contactName
    var longClicked = false

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    activeLongClick.value = null
                    longClicked = false
                    delay(viewConfiguration.longPressTimeoutMillis)
                    activeLongClick.value = contactName
                    longClicked = true
                }

                is PressInteraction.Release -> {
                    if (!longClicked) {
                        val intent = Intent(context, ChatActivity::class.java)
                            .putExtra("com.github.amitbashan.sms.originatingAddress", contactName)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    FilledTonalButton(
        onClick = {},
        shape = RoundedCornerShape(10.dp),
        interactionSource = interactionSource
    ) {
        if (isLongClick) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                ) {
                    Text(contactName)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (timeOfLastMessage.toLocalDate() == LocalDate.now()) {
                                timeOfLastMessage.toLocalTime()
                                    .format(DateTimeFormatter.ofPattern("hh:mm"))
                            } else {
                                timeOfLastMessage.format(ISO_LOCAL_DATE)
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Row {
                    Button(
                        onClick = { activeLongClick.value = null },
                        contentPadding = PaddingValues(0.dp),
                        shape = AbsoluteRoundedCornerShape(25f, 0f, 0f, 25f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close button group")
                    }
                    Button(
                        onClick = {
                            activeLongClick.value = null
                            blockOnclickHandler(contactName)
                        },
                        contentPadding = PaddingValues(0.dp),
                        shape = AbsoluteRoundedCornerShape(0f, 25f, 25f, 0f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            if (isSpamActivity) {
                                Icons.Default.Check
                            } else {
                                Icons.Default.Block
                            },
                            contentDescription = if (isSpamActivity) {
                                "Unblock contact"
                            } else {
                                "Block contact"
                            }
                        )
                    }
                }
            }
        } else {
            Column {
                Text(contactName)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (timeOfLastMessage.toLocalDate() == LocalDate.now()) {
                            timeOfLastMessage.toLocalTime()
                                .format(DateTimeFormatter.ofPattern("hh:mm"))
                        } else {
                            timeOfLastMessage.format(ISO_LOCAL_DATE)
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        lastMessage,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}