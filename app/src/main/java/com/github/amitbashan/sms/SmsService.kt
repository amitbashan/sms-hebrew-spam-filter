package com.github.amitbashan.sms

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.github.amitbashan.sms.persistence.AppDatabase

class SmsService : Service() {
    companion object {
        private val smsReceiver = SmsReceiver()
    }

    override fun onCreate() {
        super.onCreate()
        AppDatabase.initialize(applicationContext)
        registerReceiver(
            smsReceiver,
            IntentFilter(android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION),
            RECEIVER_EXPORTED
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}