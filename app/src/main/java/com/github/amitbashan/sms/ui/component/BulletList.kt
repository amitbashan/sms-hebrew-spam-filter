package com.github.amitbashan.sms.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BulletList(listItems: List<String>) {
    listItems.forEach {
        Row {
            Text(text = "\u2022 ")
            Text(text = it)
        }
    }
}