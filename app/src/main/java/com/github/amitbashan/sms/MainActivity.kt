package com.github.amitbashan.sms

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.fonts.FontStyle
import android.os.Bundle
import android.os.PersistableBundle
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
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.amitbashan.sms.activity.AboutActivity
import com.github.amitbashan.sms.activity.ChatActivity
import com.github.amitbashan.sms.persistence.AppDatabase
import com.github.amitbashan.sms.persistence.Contact
import com.github.amitbashan.sms.persistence.ContactPreview
import com.github.amitbashan.sms.ui.component.AddContactDialog
import com.github.amitbashan.sms.ui.component.ContactButton
import com.github.amitbashan.sms.ui.component.ContactList
import com.github.amitbashan.sms.ui.component.DrawerSheet
import com.github.amitbashan.sms.ui.component.ErrorPage
import com.github.amitbashan.sms.ui.component.SearchBarInputField
import com.github.amitbashan.sms.viewmodel.CommonViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        AppDatabase.initialize(applicationContext)
        startService(Intent(applicationContext, SmsService::class.java))

        if (!hasPermissions()) {
            return setContent {
                ErrorPage("Permissions to read/send SMS were not granted...")
            }
        }

        enableEdgeToEdge()
        setContent {
            val showAddContactDialog = remember { mutableStateOf(false) }
            val addContactTextFieldValue = remember { mutableStateOf("") }
            val scope = rememberCoroutineScope()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val previews by (viewModel.db ?: return@setContent).contactPreviewDao().getAll(false)
                .collectAsState(initial = emptyList())

            ModalNavigationDrawer(
                drawerContent = {
                    DrawerSheet(
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
                            inputField = { SearchBarInputField(scope, drawerState) },
                            expanded = false,
                            onExpandedChange = {}
                        ) { }
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
                        val db = viewModel.db ?: return@AddContactDialog;
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
                    ContactList(innerPadding, previews, buttonOnClick = {
                        val intent = Intent(applicationContext, ChatActivity::class.java)
                            .putExtra("com.github.amitbashan.sms.originatingAddress", it)
                        startActivity(intent)
                    })
                }
            }
        }
    }
}
