package com.github.amitbashan.sms.ui.component

import android.telephony.PhoneNumberUtils
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddContactDialog(showDialog: MutableState<Boolean>, addContactTextFieldValue: MutableState<String>, onClick: () -> Unit) {
    if (showDialog.value) {
        val focusManager = LocalFocusManager.current

        Dialog(onDismissRequest = {
            showDialog.value = false
            addContactTextFieldValue.value = ""
            focusManager.clearFocus()
        }) {
            Card(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
            ) {
                TextField(addContactTextFieldValue.value, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Contact address...") }, onValueChange = { addContactTextFieldValue.value = it })
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    enabled = PhoneNumberUtils.isGlobalPhoneNumber(addContactTextFieldValue.value)) {
                    Text("Add")
                }
            }
        }
    }
}