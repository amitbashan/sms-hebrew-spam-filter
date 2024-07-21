package com.github.amitbashan.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
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
    @OptIn(DelicateCoroutinesApi::class) // Must run globally; there's no teardown callback.
    GlobalScope.launch(context) {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}

class SmsReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION) == true) return
        val broadcasts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val groupedBroadcasts = broadcasts.groupBy { broadcast -> broadcast.displayOriginatingAddress }
        val sortedGroupedBroadcasts = groupedBroadcasts.mapValues { it.value.sortedBy { v -> v.timestampMillis } }
        val pairs = sortedGroupedBroadcasts.map { kv ->
            val sender = kv.key
            val body: String = kv.value.fold("") { acc, x -> acc + x.messageBody }
            val timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(kv.value.first().timestampMillis), ZoneId.systemDefault())
            val message = Message(sender, timestamp, body)
            val preview = ContactPreview(sender, timestamp, body)
            Pair(message, preview)
        }

        val db = AppDatabase.getInstance()
        val contactDao = db.contactDao()
        val messageDao = db.messageDao()
        val previewDao = db.contactPreviewDao()

        goAsync {
            pairs.forEach {
                pair ->
                    val originatingAddress = pair.first.originatingAddress
                    contactDao.upsert(Contact(originatingAddress, false))
                    messageDao.pushMessage(pair.first)
                    previewDao.upsert(pair.second)
            }
        }
    }
}