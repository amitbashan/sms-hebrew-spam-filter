package com.github.amitbashan.sms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.github.amitbashan.sms.persistence.AppDatabase
import com.github.amitbashan.sms.persistence.Contact
import com.github.amitbashan.sms.persistence.ContactPreview
import com.github.amitbashan.sms.persistence.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun BroadcastReceiver.goAsync(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.launch(context) {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}

class SmsReceiver : BroadcastReceiver() {
    companion object {
        private val CHANNEL_ID = "com.github.amitbashan.sms.NOTIF_CHANNEL"
        private val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            "SMSBREW_NOTIF_CHANNEL",
            NotificationManager.IMPORTANCE_HIGH
        )
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val broadcasts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val groupedBroadcasts = broadcasts.groupBy { it.displayOriginatingAddress }
        val sortedGroupedBroadcasts =
            groupedBroadcasts.mapValues { it.value.sortedBy { it.timestampMillis } }
        val pairs = sortedGroupedBroadcasts.map { kv ->
            val sender = kv.key
            val body: String = kv.value.fold("") { acc, x -> acc + x.messageBody }
            val timestamp = System.currentTimeMillis()
            val message = Message(sender, timestamp, body, false, null)
            val preview = ContactPreview(sender, timestamp, body)
            Pair(message, preview)
        }
        val db = AppDatabase.getInstance() ?: return
        val contactDao = db.contactDao()
        val messageDao = db.messageDao()
        val previewDao = db.contactPreviewDao()

        goAsync {
            pairs.forEach {
                val originatingAddress = it.first.originatingAddress
                contactDao.insertIfDoesntExist(originatingAddress, false)
                messageDao.pushMessage(it.first)
                previewDao.upsert(it.second)
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle(it.first.originatingAddress)
                    .setContentText(it.first.content)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(it.first.content))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(notificationChannel)
                notificationManager.notify(
                    (System.currentTimeMillis() % Int.MAX_VALUE.toLong()).toInt(),
                    builder.build()
                )
            }
        }
    }
}