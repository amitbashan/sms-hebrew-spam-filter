package com.github.amitbashan.sms

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.github.amitbashan.sms.persistence.AppDatabase
import com.github.amitbashan.sms.persistence.Message
import com.github.amitbashan.sms.persistence.MessageStatus
import kotlinx.coroutines.flow.count
import java.util.HashMap
import java.util.Timer
import java.util.TimerTask

class SmsService : Service() {
    companion object {
        private val smsReceiver = SmsReceiver()
        const val TIMEOUT_PERIOD_MINS = 1L
        const val TIMEOUT_PERIOD_MINS_MILIS = TIMEOUT_PERIOD_MINS * 1000L * 60L
        const val SMS_SENT_ACTION = "com.github.amitbashan.sms.SMS_SENT_ACTION"
        const val SMS_DELIVERED_ACTION = "com.github.amitbashan.sms.SMS_DELIVERED_ACTION"
        private val timeoutTimer = Timer()
        private var smsSentCountMap: HashMap<Pair<String, Long>, Int> = HashMap()
        private var smsDeliveredCountMap: HashMap<Pair<String, Long>, Int> = HashMap()
        private var smsSentTimeoutMap: HashMap<Pair<String, Long>, Long> = HashMap()
        private var smsDeliveredTimeoutMap: HashMap<Pair<String, Long>, Long> = HashMap()
        private val smsSentBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val db = AppDatabase.getInstance() ?: return
                val originatingAddress = intent?.getStringExtra("com.github.amitbashan.sms.originatingAddress") ?: return
                val timestamp = intent.getLongExtra("com.github.amitbashan.sms.timestamp", Long.MAX_VALUE)
                val numParts = intent.getIntExtra("com.github.amitbashan.sms.numParts", Int.MAX_VALUE)
                val key = Pair(originatingAddress, timestamp)

                if (!smsSentCountMap.containsKey(key)) {
                    smsSentTimeoutMap[key] = System.currentTimeMillis()
                }

                smsSentCountMap[key] = smsSentCountMap.getOrDefault(key, 0) + 1
                val count = smsSentCountMap[key]

                if (count == numParts) {
                    smsSentCountMap.remove(key)
                    goAsync {
                        db.messageDao()
                            .updateMessageStatus(originatingAddress, timestamp, MessageStatus.Sent)
                    }
                }
            }
        }
        private val smsDeliveredBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val db = AppDatabase.getInstance() ?: return
                val originatingAddress = intent?.getStringExtra("com.github.amitbashan.sms.originatingAddress") ?: return
                val timestamp = intent.getLongExtra("com.github.amitbashan.sms.timestamp", Long.MAX_VALUE)
                val numParts = intent.getIntExtra("com.github.amitbashan.sms.numParts", Int.MAX_VALUE)
                val key = Pair(originatingAddress, timestamp)
                smsSentCountMap[key] = smsSentCountMap.getOrDefault(key, 0) + 1
                val count = smsSentCountMap[key]

                if (!smsDeliveredTimeoutMap.containsKey(key)) {
                    smsDeliveredTimeoutMap[key] = System.currentTimeMillis()
                }

                if (count == numParts) {
                    smsDeliveredCountMap.remove(key)
                    goAsync {
                        db.messageDao()
                            .updateMessageStatus(originatingAddress, timestamp, MessageStatus.Delivered)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppDatabase.initialize(applicationContext)
        registerReceiver(
            smsReceiver,
            IntentFilter(android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION),
            RECEIVER_EXPORTED
        )
        registerReceiver(smsSentBroadcastReceiver, IntentFilter(SMS_SENT_ACTION), RECEIVER_EXPORTED)
        registerReceiver(smsDeliveredBroadcastReceiver, IntentFilter(SMS_DELIVERED_ACTION), RECEIVER_EXPORTED)
        timeoutTimer.schedule(
            object : TimerTask() {
                override fun run() {
                    val timestamp = System.currentTimeMillis()
                    smsSentTimeoutMap = smsSentTimeoutMap.filterNot { timestamp - it.value > TIMEOUT_PERIOD_MINS_MILIS } as HashMap<Pair<String, Long>, Long>
                    smsDeliveredTimeoutMap = smsDeliveredTimeoutMap.filterNot { timestamp - it.value > TIMEOUT_PERIOD_MINS_MILIS } as HashMap<Pair<String, Long>, Long>
                }
            },
            0,
            TIMEOUT_PERIOD_MINS_MILIS
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsReceiver)
        unregisterReceiver(smsSentBroadcastReceiver)
        unregisterReceiver(smsDeliveredBroadcastReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}