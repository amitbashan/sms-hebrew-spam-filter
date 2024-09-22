package com.github.amitbashan.sms.viewmodel

import android.app.Application
import android.telephony.SmsManager
import androidx.lifecycle.AndroidViewModel

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    val smsManager = application.applicationContext.getSystemService(SmsManager::class.java)
}