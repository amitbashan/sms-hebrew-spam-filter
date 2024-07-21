package com.github.amitbashan.sms.viewmodel

import androidx.lifecycle.ViewModel
import com.github.amitbashan.sms.persistence.AppDatabase

class CommonViewModel : ViewModel() {
    var db: AppDatabase = AppDatabase.getInstance()
}