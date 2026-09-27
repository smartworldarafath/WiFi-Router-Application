package com.example.feedback

import android.content.Context
import android.os.Build
import com.example.data.AppPreferences
import com.example.performance.RefreshRateManager
import com.example.update.GitHubReleaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class FeedbackType(val label: String) {
    FEEDBACK("Feedback"),
    FEATURE_REQUEST("Feature Request"),
    BUG_REPORT("Bug Report")
}

sealed class FeedbackSubmissionState {
    data object Idle : FeedbackSubmissionState()
    data object Submitting : FeedbackSubmissionState()
    data class Success(val message: String) : FeedbackSubmissionState()
    data class Error(val message: String, val needsConfig: Boolean = false) : FeedbackSubmissionState()
}

class FeedbackService(private val context: Context) {

    private val appPreferences = AppPreferences(context)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun sendFeedback(
        type: FeedbackType,
        userMessage: String,
        includeDeviceInfo: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        if (userMessage.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a message."))
        }

        val formattedHeader = "App: Netis Router | Version: ${GitHubReleaseService.CURRENT_APP_VERSION} | Type: ${type.label} | Message: $userMessage"
        val fullText = if (includeDeviceInfo) {
            val deviceInfo = getDeviceSummary()
            "$formattedHeader\n\n[Diagnostics Auto-Attached]\n$deviceInfo"
        } else {
            formattedHeader
        }

        // Check user-configured credentials from AppPreferences first, then fallback to embedded
        val userToken = appPreferences.telegramBotTokenFlow.first()
        val userChatId = appPreferences.telegramChatIdFlow.first()

        val botToken = if (userToken.isNotBlank()) userToken else assembleSecureToken()
        val chatId = if (userChatId.isNotBlank()) userChatId else assembleSecureChatId()

        if (botToken.isBlank() || chatId.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Telegram Bot configuration missing. Please enter your Bot Token & Chat ID.")
            )
        }

        try {
            val endpoint = "https://api.telegram.org/bot$botToken/sendMessage"
            val jsonPayload = JSONObject().apply {
                put("chat_id", chatId)
                put("text", fullText)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success("Feedback submitted successfully! Thank you for helping improve Netis Router.")
            } else {
                val errorMsg = try {
                    JSONObject(responseBody).optString("description", "Server error code ${response.code}")
                } catch (_: Exception) {
                    "HTTP ${response.code}"
                }
                if (response.code == 404 || errorMsg.contains("Not Found", ignoreCase = true) || errorMsg.contains("Unauthorized", ignoreCase = true)) {
                    Result.failure(Exception("Telegram Bot token invalid or expired ($errorMsg). Please configure your active Telegram Bot Token & Chat ID."))
                } else {
                    Result.failure(Exception("Telegram Notice: $errorMsg"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message ?: "Unable to connect"}"))
        }
    }

    private fun getDeviceSummary(): String {
        val refreshInfo = RefreshRateManager.getDisplayRefreshInfo(context)
        return "Device: ${Build.MANUFACTURER} ${Build.MODEL} | Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) | Brand: ${Build.BRAND} | Display: ${refreshInfo.currentRefreshRate}Hz (${refreshInfo.displayModeName})"
    }

    private fun assembleSecureToken(): String {
        val key = byteArrayOf(0x5A.toByte(), 0x3C.toByte(), 0x7E.toByte(), 0x1F.toByte())
        val encodedBytes = byteArrayOf(
            0x62, 0x0D, 0x4C, 0x2C, 0x6E, 0x09, 0x49, 0x28, 0x63, 0x06,
            0x44, 0x21, 0x3F, 0x7D, 0x3F, 0x57, 0x1B, 0x54, 0x1A, 0x72
        )
        val decoded = StringBuilder()
        for (i in encodedBytes.indices) {
            val mask = key[i % key.size].toInt()
            val charCode = (encodedBytes[i].toInt() xor mask) and 0xFF
            decoded.append(charCode.toChar())
        }
        return decoded.toString()
    }

    private fun assembleSecureChatId(): String {
        val key = byteArrayOf(0x2B.toByte(), 0x64.toByte())
        val encodedBytes = byteArrayOf(
            0x1B, 0x57, 0x1E, 0x51, 0x19, 0x50, 0x18, 0x55, 0x1F
        )
        val decoded = StringBuilder()
        for (i in encodedBytes.indices) {
            val mask = key[i % key.size].toInt()
            val charCode = (encodedBytes[i].toInt() xor mask) and 0xFF
            decoded.append(charCode.toChar())
        }
        return decoded.toString()
    }
}
