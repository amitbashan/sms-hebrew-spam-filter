package com.github.amitbashan.sms.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun MainBottomBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        OutlinedButton(onClick = { /*TODO*/ }, shape = RectangleShape) {
            Icon(Icons.Default.Add, contentDescription = "Add contact")
            Text("Add contact")
        }
        VerticalDivider()
        OutlinedButton(onClick = { /*TODO*/ }, shape = RectangleShape) {
            Icon(Icons.Default.Warning, contentDescription = "Add contact")
            Text("Spam contacts")
        }
    }
}