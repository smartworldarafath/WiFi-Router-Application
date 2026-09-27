package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.ui.components.DockToggle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.feedback.FeedbackService
import com.example.feedback.FeedbackSubmissionState
import com.example.feedback.FeedbackType
import com.example.ui.theme.NetisSuccess
import kotlinx.coroutines.launch

@Composable
fun FeedbackDrawerScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appPreferences = remember { AppPreferences(context) }
    val feedbackService = remember { FeedbackService(context) }

    val savedAttachDiagnostics by appPreferences.autoAttachDiagnosticsFlow.collectAsState(initial = true)
    val savedBotToken by appPreferences.telegramBotTokenFlow.collectAsState(initial = "")
    val savedChatId by appPreferences.telegramChatIdFlow.collectAsState(initial = "")

    var selectedType by remember { mutableStateOf(FeedbackType.FEEDBACK) }
    var messageText by remember { mutableStateOf("") }
    var attachDeviceInfo by remember { mutableStateOf(true) }
    var submissionState by remember { mutableStateOf<FeedbackSubmissionState>(FeedbackSubmissionState.Idle) }

    var showBotConfig by remember { mutableStateOf(false) }
    var inputBotToken by remember { mutableStateOf(savedBotToken) }
    var inputChatId by remember { mutableStateOf(savedChatId) }

    // Sync state with saved preferences
    LaunchedEffect(savedAttachDiagnostics) {
        attachDeviceInfo = savedAttachDiagnostics
    }
    LaunchedEffect(savedBotToken, savedChatId) {
        if (inputBotToken.isEmpty()) inputBotToken = savedBotToken
        if (inputChatId.isEmpty()) inputChatId = savedChatId
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("feedback_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Send Feedback & Requests",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Help make Netis Router better. Your submission is securely relayed directly to the developer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Feedback Type Segmented Selector
        item {
            Text(
                text = "Feedback Type",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeedbackType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    val icon = when (type) {
                        FeedbackType.FEEDBACK -> Icons.Default.Feedback
                        FeedbackType.FEATURE_REQUEST -> Icons.Default.Lightbulb
                        FeedbackType.BUG_REPORT -> Icons.Default.BugReport
                    }
                    val borderCol by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        animationSpec = spring(),
                        label = "feedbackTypeBorder"
                    )

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedType = type }
                            .testTag("type_${type.name.lowercase()}"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, borderCol),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Message Input Field
        item {
            Column {
                Text(
                    text = "Message Details",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .testTag("feedback_message_input"),
                    placeholder = {
                        Text(
                            when (selectedType) {
                                FeedbackType.FEEDBACK -> "Tell us about your experience with the app or router management..."
                                FeedbackType.FEATURE_REQUEST -> "Describe the feature or tool you'd like added to Netis Router..."
                                FeedbackType.BUG_REPORT -> "Describe what went wrong, router model, and steps to reproduce..."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${messageText.length} characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        // Auto-attach Device Info Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-attach device diagnostics",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Includes device model, Android OS level, and display refresh rate to diagnose router issues.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DockToggle(
                        checked = attachDeviceInfo,
                        onCheckedChange = {
                            attachDeviceInfo = it
                            scope.launch { appPreferences.saveAutoAttachDiagnostics(it) }
                        },
                        testTag = "attach_device_info_dock_toggle"
                    )
                }
            }
        }

        // Submit Button & Submission States
        item {
            val isSubmitting = submissionState is FeedbackSubmissionState.Submitting
            val isSuccess = submissionState is FeedbackSubmissionState.Success

            Button(
                onClick = {
                    if (messageText.isBlank()) return@Button
                    submissionState = FeedbackSubmissionState.Submitting
                    scope.launch {
                        val result = feedbackService.sendFeedback(
                            type = selectedType,
                            userMessage = messageText,
                            includeDeviceInfo = attachDeviceInfo
                        )
                        result.fold(
                            onSuccess = { msg ->
                                submissionState = FeedbackSubmissionState.Success(msg)
                                messageText = ""
                            },
                            onFailure = { err ->
                                submissionState = FeedbackSubmissionState.Error(
                                    message = err.message ?: "Submission failure",
                                    needsConfig = true
                                )
                            }
                        )
                    }
                },
                enabled = messageText.isNotBlank() && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("send_feedback_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSuccess) NetisSuccess else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Submitting...")
                } else if (isSuccess) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sent Successfully!")
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Feedback")
                }
            }
        }

        // Status Banners
        item {
            when (val state = submissionState) {
                is FeedbackSubmissionState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = NetisSuccess.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NetisSuccess
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                is FeedbackSubmissionState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Notice",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { showBotConfig = !showBotConfig },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bot Setup", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "Netis Router Feedback: ${selectedType.label}")
                                            putExtra(Intent.EXTRA_TEXT, messageText)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Feedback"))
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share Text", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        // Telegram Bot Configuration Card
        if (showBotConfig || savedBotToken.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Telegram Bot Configuration",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configure your custom Telegram bot token & chat ID to relay feedback directly to your bot.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = inputBotToken,
                            onValueChange = { inputBotToken = it },
                            label = { Text("Bot Token (e.g. 123456789:ABC...)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = inputChatId,
                            onValueChange = { inputChatId = it },
                            label = { Text("Chat ID (e.g. -1001234567 or 12345678)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                scope.launch {
                                    appPreferences.saveTelegramCredentials(inputBotToken, inputChatId)
                                    Toast.makeText(context, "Telegram Bot credentials saved!", Toast.LENGTH_SHORT).show()
                                    showBotConfig = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Telegram Credentials")
                        }
                    }
                }
            }
        }
    }
}
