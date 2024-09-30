package com.github.amitbashan.sms.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.amitbashan.sms.persistence.ContactPreview
import java.time.Instant
import java.time.ZoneId

@Composable
fun ContactList(innerPadding: PaddingValues,
                previews: List<ContactPreview>,
                buttonOnClick: (String) -> Unit,
                blockOnclick: (String) -> Unit,
                activeLongClick: MutableState<Int?>,
                isSpamActivity: Boolean,
                ) {
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
            itemsIndexed(sortedPreviews) { index, item ->
                val timestamp =
                    Instant.ofEpochMilli(item.timestamp).atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                ContactButton(
                    item.originatingAddress,
                    item.content.orEmpty(),
                    timestamp,
                    index,
                    activeLongClick,
                    onclickHandler =
                    {
                        buttonOnClick(item.originatingAddress)
                    },
                    blockOnclickHandler = blockOnclick,
                    isSpamActivity
                )
            }
        }
    }
}