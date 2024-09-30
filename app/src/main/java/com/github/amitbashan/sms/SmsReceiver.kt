package com.github.amitbashan.sms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.github.amitbashan.sms.activity.ChatActivity
import com.github.amitbashan.sms.persistence.AppDatabase
import com.github.amitbashan.sms.persistence.ContactPreview
import com.github.amitbashan.sms.persistence.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
            "SMSBrew",
            NotificationManager.IMPORTANCE_HIGH
        )
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
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

                val activityIntent = Intent(context, ChatActivity::class.java)
                    .putExtra("com.github.amitbashan.sms.originatingAddress", originatingAddress)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    activityIntent,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle(originatingAddress)
                    .setContentText(it.first.content)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(it.first.content))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)

                notificationManager.notify(
                    (System.currentTimeMillis() % Int.MAX_VALUE.toLong()).toInt(),
                    builder.build()
                )
            }
        }
    }
}