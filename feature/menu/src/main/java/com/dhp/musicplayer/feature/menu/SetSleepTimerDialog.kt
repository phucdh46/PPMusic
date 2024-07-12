package com.dhp.musicplayer.feature.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.dialog.DefaultDialog

@Composable
fun SetSleepTimerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    title: String? = null,
) {
    var hoursText by remember { mutableStateOf(TextFieldValue()) }
    var minutesText by remember { mutableStateOf(TextFieldValue()) }
    var timeSleep by remember { mutableStateOf<Int?>(null) }

    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(hoursText,minutesText ) {
        timeSleep = (hoursText.text.toIntOrNull()
            ?: 0) * 60 + (minutesText.text.toIntOrNull() ?: 0)
    }
    DefaultDialog(
        onDismiss = onDismiss,
        onConfirm = {
            timeSleep?.let { onConfirm(it) }
                    },
        title = title,
        isError = (timeSleep == null || timeSleep!! <=  0)
    ) {
        Column {
            BasicText(
                text = stringResource(R.string.set_sleep_timer_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 24.dp)
            )
            OutlinedTextField(
                value = hoursText,
                onValueChange = {
                    val filteredHoursText = filterValidInput(it.text, 23)
                    hoursText = TextFieldValue(
                        text = filteredHoursText,
                        selection = TextRange(filteredHoursText.length)
                    )
                },
                label = { Text(text = stringResource(R.string.sleep_timer_enter_hours_text)) },
                placeholder = { Text(text = stringResource(R.string.sleep_timer_enter_hours_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.focusRequester(focusRequester)
            )


            OutlinedTextField(
                value = minutesText,
                onValueChange = {
                    val filteredMinutesText = filterValidInput(it.text, 59)
                    minutesText = TextFieldValue(
                        text = filteredMinutesText,
                        selection = TextRange(filteredMinutesText.length)
                    )
                },
                label = { Text(text = stringResource(R.string.sleep_timer_enter_minutes_text)) },
                placeholder = { Text(text = stringResource(R.string.sleep_timer_enter_minutes_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}

fun filterValidInput(input: String, max: Int): String {
    val number = input.toIntOrNull()
    return when {
        number == null -> ""
        number < 0 -> "0"
        number > max -> max.toString()
        else -> input
    }
}