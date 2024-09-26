package com.github.amitbashan.sms.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.amitbashan.sms.persistence.ContactPreview
import java.time.Instant
import java.time.ZoneId

@Composable
fun ContactList(innerPadding: PaddingValues, previews: List<ContactPreview>, buttonOnClick: (String) -> Unit) {
    val sortedPreviews = previews.sortedByDescending { preview -> preview.timestamp }
    if (sortedPreviews.isEmpty()) {
        ErrorPage("No SMS messages have been received or sent yet...")
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .absolutePadding(left = 5.dp, right = 5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            items(sortedPreviews) {
                val timestamp =
                    Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                ContactButton(
                    it.originatingAddress,
                    it.content.orEmpty(),
                    timestamp,
                    onclickHandler =
                    {
                        buttonOnClick(it.originatingAddress)
                    }
                )
            }
        }
    }
}