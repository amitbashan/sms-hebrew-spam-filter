package com.github.amitbashan.sms.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AboutActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
                    .imePadding()
                    .navigationBarsPadding(),
                topBar = {
                    TopAppBar(
                        title = {
                            Text(buildAnnotatedString {
                                append("About ")
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append("SMS")
                                }
                                append("Brew")
                            }, fontSize = 24.sp, modifier = Modifier.padding(16.dp))
                        },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Go back"
                                )
                            }
                        })
                }
            ) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    Text(
                        "SMSBrew is an SMS app with Hebrew spam filtering capabilities powered by a fine-tuned BERT model, along with a database of malicious URL patterns.\nSMSBrew was developed by:",
                        fontSize = 16.sp,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.absolutePadding(top = 10.dp, bottom = 10.dp)) {
                        listOf("Amit Bashan (@amitbashan)", "Hila Damin (@hida10)", "Tomer Sasson (@asasson)").forEach {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 16.dp, end = 8.dp)
                                        .size(4.dp)
                                        .background(Color.Black, shape = CircleShape),
                                )
                                Text(it, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Text(
                        buildAnnotatedString {
                            append("as a final project for the BSc computer science program at ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append("Netanya Academic College")
                            }
                            append('.')
                        },
                        fontSize = 16.sp
                    )
                    HorizontalDivider(modifier = Modifier.padding(8.dp))
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append("Repository: ")
                            }
                            withStyle(style = SpanStyle(color = Color.Blue)) {
                                append("https://github.com/amitbashan/sms-hebrew-spam-filter")
                            }
                            append('\n')
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append("Advisor: Dr. Gershon Kagan")
                            }
                        }
                    )
                }
            }
        }
    }
}