package com.github.amitbashan.sms.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarInputField(
    scope: CoroutineScope,
    drawerState: DrawerState,
    text: MutableStateFlow<String>,
    activeLongClick: MutableState<String?>,
) {
    val query by text.collectAsState(initial = "")

    SearchBarDefaults.InputField(
        query,
        onQueryChange = {
            activeLongClick.value = null
            text.value = it
        },
        onSearch = { },
        expanded = false,
        onExpandedChange = { },
        placeholder = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text("Search contacts...")
            }
        },
        leadingIcon = {
            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = null
                )
            }
        },
        trailingIcon = {
//            IconButton(onClick = { /*TODO*/ }) {
//                Icon(
//                    Icons.Default.MoreVert,
//                    contentDescription = null
//                )
//            }
        },
    )
}