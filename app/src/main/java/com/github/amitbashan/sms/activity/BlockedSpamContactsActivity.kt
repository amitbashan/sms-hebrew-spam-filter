package com.github.amitbashan.sms.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.github.amitbashan.sms.ui.component.ContactList
import com.github.amitbashan.sms.viewmodel.CommonViewModel
import kotlinx.coroutines.launch

class BlockedSpamContactsActivity : ComponentActivity() {
    private val viewModel: CommonViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = viewModel.db ?: return;

        setContent {
            val previews by db.contactPreviewDao().getAll(true)
                .collectAsState(initial = emptyList())
            val activeLongClick: MutableState<Int?> = remember { mutableStateOf(null) }

            Scaffold(
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding(),
                topBar = {
                    TopAppBar(title = { Text("Blocked & spam contacts") }, navigationIcon = {
                        IconButton(onClick = {
                            finish()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go back"
                            )
                        }
                    })
                },
            ) { innerPadding ->
                ContactList(
                    innerPadding, previews, buttonOnClick = {
                        val intent = Intent(applicationContext, ChatActivity::class.java)
                            .putExtra("com.github.amitbashan.sms.originatingAddress", it)
                        startActivity(intent)
                    },
                    blockOnclick = {
                        lifecycleScope.launch {
                            db.contactDao().setSpamStatus(it, false)
                        }
                    },
                    activeLongClick,
                    true
                )
            }
        }
    }
}