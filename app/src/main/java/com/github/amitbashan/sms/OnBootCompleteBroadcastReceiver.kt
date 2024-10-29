package com.github.amitbashan.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class OnBootCompleteBroadcastReceiver : BroadcastReceiver()  {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, SmsReceiver::class.java)
            context.startService(serviceIntent)
            println("Started SMSBrew service")
        }
    }
}