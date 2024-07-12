package com.dhp.musicplayer.feature.settings.feedback

import android.util.Patterns
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.core.common.extensions.toast
import com.dhp.musicplayer.core.common.utils.Logg
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.component.TopAppBarDetailScreen
import com.dhp.musicplayer.core.ui.LocalWindowInsets
import com.dhp.musicplayer.core.ui.common.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onBackClick: () -> Unit,
    viewModel: FeedbackViewModel = hiltViewModel()
) {

    val message by viewModel.message.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    var feedback by remember { mutableStateOf(TextFieldValue()) }
    var name by remember { mutableStateOf(TextFieldValue()) }
    var email by remember { mutableStateOf(TextFieldValue()) }
    var feedbackError by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    message?.let {
        context.toast(it)
        onBackClick()
    }

    LaunchedEffect(feedback, submitted) {
        feedbackError = submitted && feedback.text.isEmpty()
    }

    LaunchedEffect(name, submitted) {
        nameError = submitted && name.text.isEmpty()
    }

    LaunchedEffect(email, submitted) {
        emailError =
            submitted && (email.text.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email.text)
                .matches())
        Logg.d(
            "emailError: ${email.text} - $emailError - ${
                Patterns.EMAIL_ADDRESS.matcher(email.text).matches()
            }"
        )
    }


    fun validateInputs(): Boolean {
        return feedback.text.isNotEmpty() &&
                name.text.isNotEmpty() &&
                email.text.isNotEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email.text).matches()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(LocalWindowInsets.current.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)),
        topBar = {
            TopAppBarDetailScreen(
                onBackClick = onBackClick,
                title = {
                    Text(
                        text = "Feedback".uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Handle send action
                submitted = true
                if (validateInputs()) {
                    viewModel.sendFeedback(feedback.text, name.text, email.text)
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = stringResource(id = R.string.app_name)
                )
            }
        }
    )
    { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            item {
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
////                    elevation = 5.dp
//                ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_feedback_input),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it },
                        label = { Text(stringResource(id = R.string.settings_feedback_input_hint)) },
                        isError = feedbackError,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                    )
                    if (feedbackError) {
                        Text(
                            text = "Feedback cannot be empty",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        text = stringResource(id = R.string.settings_feedback_name),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(id = R.string.settings_feedback_name_hint)) },
                        isError = nameError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (nameError) {
                        Text(
                            text = "Name cannot be empty",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        text = stringResource(id = R.string.settings_feedback_email),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(id = R.string.settings_feedback_email_hint)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = emailError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (emailError) {
                        Text(
                            text = "Email does not use the correct format",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
//                }
            }
        }
    }
    if (isLoading) {
        LoadingScreen(backgroundColor = Color.Transparent)
    }
}
