package com.github.amitbashan.sms.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.amitbashan.sms.persistence.ContactPreview
import kotlinx.coroutines.flow.Flow

@Composable
fun ContactList(
    innerPadding: PaddingValues,
    previewsFlow: Flow<List<ContactPreview>>,
    blockOnclick: (String) -> Unit,
    activeLongClick: MutableState<String?>,
    isSpamActivity: Boolean,
) {
    val previews by previewsFlow.collectAsState(initial = emptyList())

    if (previews.isEmpty()) {
        ErrorPage("No SMS messages have been received or sent yet...")
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .absolutePadding(left = 5.dp, right = 5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            items(previews, key = { it.originatingAddress }) {
                ContactButton(
                    it,
                    activeLongClick,
                    blockOnclickHandler = blockOnclick,
                    isSpamActivity
                )
            }
        }
    }
}