package com.github.amitbashan.sms.activity

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.github.amitbashan.sms.persistence.Message as DbMessage
import android.telephony.SmsManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import com.github.amitbashan.sms.goAsync
import com.github.amitbashan.sms.ui.component.ErrorPage
import com.github.amitbashan.sms.ui.component.Message
import com.github.amitbashan.sms.ui.component.MessageTextBox
import com.github.amitbashan.sms.viewmodel.ChatViewModel
import com.github.amitbashan.sms.viewmodel.CommonViewModel
import java.time.LocalDateTime

class ChatActivity : ComponentActivity() {
    private val viewModel: CommonViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    lateinit var originatingAddress: String

    private fun sendMessage(message: String) {
        val timestamp = LocalDateTime.now()
        val smsSentAction = "SMS_SENT_ACTION_${timestamp.hashCode()}"
        val smsDeliveredAction = "SMS_DELIVERED_ACTION_${timestamp.hashCode()}"
        val smsSentPendingIntent =
            PendingIntent.getBroadcast(applicationContext, 0, Intent(smsSentAction),
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val smsDeliveredPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, Intent(smsDeliveredAction), PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val smsSentBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                val msg = DbMessage(originatingAddress, timestamp, message, true)
                if (viewModel.db != null) {
                    goAsync {
                        viewModel.db!!.messageDao().pushMessage(msg)
                    }
                }
            }
        }
        val smsDeliveredBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                println("DELIVERED")
            }
        }

        registerReceiver(
            smsSentBroadcastReceiver,
            IntentFilter(smsSentAction),
            RECEIVER_EXPORTED
        )
        registerReceiver(
            smsDeliveredBroadcastReceiver,
            IntentFilter(smsDeliveredAction),
            RECEIVER_EXPORTED
        )
        chatViewModel.smsManager.sendTextMessage(
            originatingAddress,
            null,
            message,
            smsSentPendingIntent,
            smsDeliveredPendingIntent
        )
        unregisterReceiver(smsDeliveredBroadcastReceiver)
        unregisterReceiver(smsSentBroadcastReceiver)
    }

    @Composable
    fun MessageList(innerPadding: PaddingValues, originatingAddress: String) {
        val db = viewModel.db ?: return ErrorPage("Error: database is uninitialized")
        val conversation by db.messageDao().getConversationOf(originatingAddress)
            .collectAsState(initial = emptyList())
        return LazyColumn(
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = WindowInsets.navigationBars.asPaddingValues(),
            horizontalAlignment = Alignment.Start
        ) {
            items(conversation) { message ->
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(3.dp),
                    horizontalArrangement = if (message.isMe) {
                        Arrangement.End
                    } else { Arrangement.Start }
                ) {
                    Message(message.content, message.isMe)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        originatingAddress =
            intent.getStringExtra("contact") ?: return setContent { ErrorPage("Unable to load chat") }

        setContent {
            val message = remember { mutableStateOf("") }

            Scaffold(
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding(),
                topBar = {
                    TopAppBar(title = { Text(originatingAddress) }, navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go back"
                            )
                        }
                    })
                },
                bottomBar = {
                    MessageTextBox(
                        message,
                        onClick = {
                            sendMessage(message.value)
                        }
                    )
                }
            ) { innerPadding ->
                MessageList(innerPadding, originatingAddress)
            }
        }
    }
}