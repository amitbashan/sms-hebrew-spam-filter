package com.github.amitbashan.sms

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.amitbashan.sms.activity.ChatActivity
import com.github.amitbashan.sms.persistence.AppDatabase
import com.github.amitbashan.sms.ui.component.ContactButton
import com.github.amitbashan.sms.viewmodel.CommonViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CommonViewModel by viewModels()
    private val smsReceiver = SmsReceiver()

    @Composable
    fun ContactList(innerPadding: PaddingValues) {
        val previews by viewModel.db.contactPreviewDao().getAll()
            .collectAsState(initial = emptyList())
        val sortedPreviews = previews.sortedByDescending { preview -> preview.timestamp }
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            sortedPreviews.forEach { preview ->
                ContactButton(
                    preview.originatingAddress,
                    preview.content,
                    preview.timestamp,
                    onclickHandler =
                    {
                        val intent = Intent(applicationContext, ChatActivity::class.java)
                        intent.putExtra("contact", preview.originatingAddress)
                        startActivity(intent)
                    }
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppDatabase.initialize(applicationContext)

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECEIVE_SMS),
                0
            )
        }

        enableEdgeToEdge()
        setContent {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
            ) { innerPadding ->
                ContactList(innerPadding)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(smsReceiver, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsReceiver)
    }
}
