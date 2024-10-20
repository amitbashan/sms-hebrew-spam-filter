package com.github.amitbashan.sms.activity

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.github.amitbashan.sms.SmsReceiver
import com.github.amitbashan.sms.SmsService
import com.github.amitbashan.sms.persistence.ContactPreview
import com.github.amitbashan.sms.ui.component.ChatInput
import com.github.amitbashan.sms.ui.component.ErrorPage
import com.github.amitbashan.sms.ui.component.Message
import com.github.amitbashan.sms.viewmodel.ChatViewModel
import com.github.amitbashan.sms.viewmodel.CommonViewModel
import kotlinx.coroutines.launch
import com.github.amitbashan.sms.persistence.Message as DbMessage

class ChatActivity : ComponentActivity() {
    private val viewModel: CommonViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    private fun sendMessage(originatingAddress: String, message: String) {
        val db = viewModel.db ?: return
        val timestamp = System.currentTimeMillis()
        val divMessage = chatViewModel.smsManager.divideMessage(message)
        val numParts = divMessage.size
        val smsSentPendingIntents =
            (1.rangeTo(numParts)).map {
                PendingIntent.getBroadcast(
                    this,
                    it,
                    Intent(SmsService.SMS_SENT_ACTION)
                        .putExtra(
                            "com.github.amitbashan.sms.originatingAddress",
                            originatingAddress
                        )
                        .putExtra("com.github.amitbashan.sms.timestamp", timestamp)
                        .putExtra("com.github.amitbashan.sms.numParts", numParts),
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            }
        val smsDeliveredPendingIntents =
            (1.rangeTo(numParts)).map {
                PendingIntent.getBroadcast(
                    this,
                    it,
                    Intent(SmsService.SMS_DELIVERED_ACTION)
                        .putExtra(
                            "com.github.amitbashan.sms.originatingAddress",
                            originatingAddress
                        )
                        .putExtra("com.github.amitbashan.sms.timestamp", timestamp)
                        .putExtra("com.github.amitbashan.sms.numParts", numParts),
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            }

        try {
            chatViewModel.smsManager.sendMultipartTextMessage(
                originatingAddress,
                null,
                divMessage,
                ArrayList(smsSentPendingIntents),
                ArrayList(smsDeliveredPendingIntents)
            )
            val msg =
                DbMessage(originatingAddress, timestamp, message, true, null, false, 0F)
            lifecycleScope.launch {
                db.messageDao().pushMessage(msg)
                db.contactPreviewDao()
                    .upsert(ContactPreview(originatingAddress, timestamp, message))
            }
        } catch (_: Exception) {
            Toast.makeText(applicationContext, "Failed to send SMS message", Toast.LENGTH_LONG)
                .show()
        }
    }

    @Composable
    fun MessageList(innerPadding: PaddingValues, originatingAddress: String) {
        val db = viewModel.db ?: return ErrorPage("Error: database is uninitialized")
        val conversation by db.messageDao().getConversationOf(originatingAddress)
            .collectAsState(initial = emptyList())

        LazyColumn(
            Modifier
                .padding(innerPadding)
                .padding(5.dp)
                .fillMaxSize(),
            contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            horizontalAlignment = Alignment.Start
        ) {
            items(conversation) {
                Message(it.content, it.isMe, it.isSpam, it.isSpamProbability, it.messageStatus)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val originatingAddress =
            intent.getStringExtra("com.github.amitbashan.sms.originatingAddress")
                ?: return setContent { ErrorPage("Unable to load chat") }
        SmsReceiver.setActive(originatingAddress)

        setContent {
            val message = remember { mutableStateOf("") }
            val focusManager = LocalFocusManager.current

            Scaffold(
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding(),
                topBar = {
                    TopAppBar(title = { Text(originatingAddress) }, navigationIcon = {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            finish()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go back"
                            )
                        }
                    })
                },
                bottomBar = {
                    ChatInput(
                        message,
                        onClick = {
                            sendMessage(originatingAddress, message.value)
                            message.value = ""
                        }
                    )
                }
            ) { innerPadding ->
                MessageList(innerPadding, originatingAddress)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        SmsReceiver.clearActive()
    }
}