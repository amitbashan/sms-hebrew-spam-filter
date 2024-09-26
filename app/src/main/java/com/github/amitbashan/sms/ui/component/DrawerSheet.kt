package com.github.amitbashan.sms.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@Composable
fun DrawerSheet(aboutOnClick: () -> Unit) {
    ModalDrawerSheet() {
        Text(buildAnnotatedString {
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append("SMS")
            }
            append("Brew")
        }, fontSize = 24.sp, modifier = Modifier.padding(16.dp))
        HorizontalDivider()
        NavigationDrawerItem(
            modifier = Modifier.padding(20.dp),
            label = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Block,
                        contentDescription = null
                    )
                    Text(text = "Spam & blocked", fontSize = 15.sp)
                }
            },
            selected = false,
            onClick = { /*TODO*/ }
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(0.9f)
                .align(Alignment.CenterHorizontally),
            thickness = 2.dp
        )
        NavigationDrawerItem(
            modifier = Modifier.padding(20.dp),
            label = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null
                    )
                    Text(text = "About", fontSize = 15.sp)
                }
            },
            selected = false,
            onClick = aboutOnClick,
        )
    }
}