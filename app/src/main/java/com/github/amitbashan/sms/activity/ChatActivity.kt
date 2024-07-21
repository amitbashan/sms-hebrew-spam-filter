package com.github.amitbashan.sms.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.github.amitbashan.sms.persistence.AppDatabase
import com.github.amitbashan.sms.persistence.Contact
import com.github.amitbashan.sms.ui.component.Message
import com.github.amitbashan.sms.viewmodel.CommonViewModel
import kotlinx.coroutines.launch

class ChatActivity : ComponentActivity() {
    private val viewModel: CommonViewModel by viewModels()

    @Composable
    fun MessageList(innerPadding: PaddingValues, originatingAddress: String) {
        val conversation by viewModel.db.messageDao().getConversationOf(originatingAddress)
            .collectAsState(initial = emptyList())
        return LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.Start
        ) {
            conversation.forEach { message ->
                item {
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Message(message.content)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val originatingAddress =
            intent.getStringExtra("contact") ?: return setContent { Text("Unable to load chat") }
        enableEdgeToEdge()
        setContent {
            Scaffold(topBar = {
                TopAppBar(title = { Text(originatingAddress) }, navigationIcon = {
                    IconButton(onClick = { finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                })
            }) { innerPadding ->
                MessageList(innerPadding, originatingAddress)
            }
        }
    }
}