package com.github.amitbashan.sms

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.fonts.FontStyle
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.amitbashan.sms.activity.ChatActivity
import com.github.amitbashan.sms.persistence.AppDatabase
import com.github.amitbashan.sms.ui.component.BulletList
import com.github.amitbashan.sms.ui.component.ContactButton
import com.github.amitbashan.sms.ui.component.ErrorPage
import com.github.amitbashan.sms.viewmodel.CommonViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CommonViewModel by viewModels()

    fun hasPermissions(): Boolean {
        val needsReadSmsPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_DENIED
        val needsSendSmsPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_DENIED

        return !(needsReadSmsPermission || needsSendSmsPermission)
    }

    fun requestPermissions() {
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.RECEIVE_SMS,
                    android.Manifest.permission.SEND_SMS
                ),
                0
            )
        }
    }

    @Composable
    fun ContactList(innerPadding: PaddingValues) {
        val db = viewModel.db ?: return
        val previews by db.contactPreviewDao().getAll()
            .collectAsState(initial = emptyList())
        val sortedPreviews = previews.sortedByDescending { preview -> preview.timestamp }
        if (sortedPreviews.isEmpty()) {
            ErrorPage("No SMS messages have been received or sent yet...")
        } else {
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
    }

    override fun onStart() {
        super.onStart()
        AppDatabase.initialize(applicationContext)
        startService(Intent(applicationContext, SmsService::class.java))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()

        if (!hasPermissions()) {
            return setContent {
                ErrorPage("Permissions to read/send SMS were not granted...")
            }
        }

        enableEdgeToEdge()
        setContent {
            val showAppInfoDialog = remember { mutableStateOf(false) }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
                topBar = {
                    TopAppBar(
                        title = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("SMS", fontSize = 20.sp)
                                Text("with Hebrew spam filter", fontSize = 15.sp)
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                showAppInfoDialog.value = true
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "App info"
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                if (showAppInfoDialog.value) {
                    Dialog(onDismissRequest = { showAppInfoDialog.value = false }) {
                        Card(
                            modifier = Modifier
                                .height(IntrinsicSize.Min)
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(5.dp),
                            ) {
                                Text(
                                    "Degree Final Project",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Netanya Academic College")
                                Row {
                                    Text("Topic: ", fontWeight = FontWeight.Bold)
                                    Text("SMS receiver with Hebrew spam filtering")
                                }
                                Text("Developed by:", fontWeight = FontWeight.Bold)
                                BulletList(
                                    listItems = listOf(
                                        "Amit Bashan",
                                        "Tomer Sasson",
                                        "Hila Damin"
                                    )
                                )
                            }
                        }
                    }
                }
                ContactList(innerPadding)
            }
        }
    }
}
