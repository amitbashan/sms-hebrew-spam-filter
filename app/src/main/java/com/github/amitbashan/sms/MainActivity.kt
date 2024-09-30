package com.github.amitbashan.sms

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.amitbashan.sms.activity.AboutActivity
import com.github.amitbashan.sms.activity.BlockedSpamContactsActivity
import com.github.amitbashan.sms.activity.ChatActivity
import com.github.amitbashan.sms.persistence.AppDatabase
import com.github.amitbashan.sms.persistence.Contact
import com.github.amitbashan.sms.persistence.ContactPreview
import com.github.amitbashan.sms.ui.component.AddContactDialog
import com.github.amitbashan.sms.ui.component.ContactList
import com.github.amitbashan.sms.ui.component.DrawerSheet
import com.github.amitbashan.sms.ui.component.ErrorPage
import com.github.amitbashan.sms.ui.component.SearchBarInputField
import com.github.amitbashan.sms.viewmodel.CommonViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: CommonViewModel by viewModels()
    private val SEARCH_DEBOUNCE_DELAY_MILIS = 400L

    fun hasPermissions(): Boolean {
        val needsReadSmsPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_DENIED
        val needsSendSmsPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_DENIED
        val needsPostNotifPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_DENIED

        return !(needsReadSmsPermission || needsSendSmsPermission || needsPostNotifPermission)
    }

    fun requestPermissions() {
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.RECEIVE_SMS,
                    android.Manifest.permission.SEND_SMS,
                    android.Manifest.permission.POST_NOTIFICATIONS,
                ),
                0
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions()
        AppDatabase.initialize(applicationContext)
        startService(Intent(applicationContext, SmsService::class.java))

        if (!hasPermissions()) {
            return setContent {
                ErrorPage("Permissions to read/send SMS were not granted...")
            }
        }

        val db = viewModel.db ?: return
        val searchText = MutableStateFlow("")
        val previewsFlow = searchText.debounce(SEARCH_DEBOUNCE_DELAY_MILIS)
            .distinctUntilChanged()
            .flatMapLatest {
                if (it.isBlank() || !"[a-zA-Z0-9 ]+".toRegex().matches(it)) {
                    db.contactPreviewDao().getAll(false)
                } else {
                    db.contactPreviewDao().searchLike("%${it}%")
                }
            }

        setContent {
            val activeLongClick: MutableState<Int?> = remember { mutableStateOf(null) }
            val showAddContactDialog = remember { mutableStateOf(false) }
            val addContactTextFieldValue = remember { mutableStateOf("") }
            val scope = rememberCoroutineScope()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val previews by previewsFlow.collectAsState(initial = emptyList())

            ModalNavigationDrawer(
                drawerContent = {
                    DrawerSheet(
                        blockAndSpamContactsOnClick = {
                            val intent =
                                Intent(applicationContext, BlockedSpamContactsActivity::class.java)
                            startActivity(intent)
                        },
                        aboutOnClick = {
                            val intent = Intent(applicationContext, AboutActivity::class.java)
                            startActivity(intent)
                        }
                    )
                }, drawerState = drawerState
            ) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                        .imePadding()
                        .navigationBarsPadding(),
                    topBar = {
                        SearchBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .absolutePadding(5.dp, 7.dp, 5.dp, 10.dp),
                            inputField = {
                                SearchBarInputField(
                                    scope,
                                    drawerState,
                                    searchText,
                                    activeLongClick
                                )
                            },
                            expanded = false,
                            onExpandedChange = { }
                        ) {}
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showAddContactDialog.value = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add contact"
                            )
                        }
                    }
                ) { innerPadding ->
                    AddContactDialog(showAddContactDialog, addContactTextFieldValue, onClick = {
                        val originatingAddress = addContactTextFieldValue.value
                        lifecycleScope.launch {
                            db.contactDao().upsert(Contact(originatingAddress, false))
                            db.contactPreviewDao().upsert(
                                ContactPreview(
                                    originatingAddress,
                                    System.currentTimeMillis(),
                                    null
                                )
                            )
                        }
                        showAddContactDialog.value = false
                    })
                    ContactList(
                        innerPadding,
                        previews,
                        buttonOnClick = {
                            val intent = Intent(applicationContext, ChatActivity::class.java)
                                .putExtra("com.github.amitbashan.sms.originatingAddress", it)
                            startActivity(intent)
                        },
                        blockOnclick = {
                            scope.launch {
                                db.contactDao().setSpamStatus(it, true)
                            }
                        },
                        activeLongClick,
                        false,
                    )
                }
            }
        }
    }
}
