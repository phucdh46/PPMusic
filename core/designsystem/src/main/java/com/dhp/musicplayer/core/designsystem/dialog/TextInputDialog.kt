package com.dhp.musicplayer.core.designsystem.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun TextInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    title: String? = null,
    label: String? = null,
    initText: String? = null
) {
    var text by remember { mutableStateOf(initText) }
    var isError by remember { mutableStateOf(false) }

    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(text) {
        isError = text?.isBlank() == true
    }

    DefaultDialog(
        onDismiss = onDismiss,
        onConfirm = { text?.let { onConfirm(it) } },
        title = title,
        isError = isError
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            value = TextFieldValue(
                text = text.orEmpty(),
                selection = TextRange(text?.length ?: 0)
            ),
            onValueChange = { text = it.text },
            label = { Text(label ?: "Name", style = MaterialTheme.typography.bodyMedium) },
            singleLine = true,
            isError = isError,
        )
    }
}