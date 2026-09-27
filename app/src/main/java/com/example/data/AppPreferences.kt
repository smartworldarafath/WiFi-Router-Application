package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.performance.AppIconOption
import com.example.performance.PerformanceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode(val title: String, val description: String) {
    SYSTEM("System Default", "Follows device system appearance"),
    LIGHT("Light Mode", "Crisp daytime high-contrast interface"),
    DARK("Dark Mode", "Deep midnight OLED interface");

    companion object {
        fun fromString(value: String?): ThemeMode {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: SYSTEM
        }
    }
}

private val Context.dataStore by preferencesDataStore(name = "netis_router_settings")

class AppPreferences(private val context: Context) {

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("app_theme_mode")
        val KEY_PERFORMANCE_MODE = stringPreferencesKey("performance_mode")
        val KEY_SELECTED_ICON = stringPreferencesKey("selected_icon")
        val KEY_ROUTER_IP = stringPreferencesKey("router_ip")
        val KEY_ROUTER_PASSWORD = stringPreferencesKey("router_password")
        val KEY_ROUTER_WEB_URL = stringPreferencesKey("router_web_url")
        val KEY_DESKTOP_MODE = booleanPreferencesKey("router_desktop_mode")
        val KEY_FORCE_HIGH_REFRESH_RATE = booleanPreferencesKey("force_high_refresh_rate")
        val KEY_TARGET_REFRESH_RATE = intPreferencesKey("target_refresh_rate")
        val KEY_TELEGRAM_BOT_TOKEN = stringPreferencesKey("telegram_bot_token")
        val KEY_TELEGRAM_CHAT_ID = stringPreferencesKey("telegram_chat_id")
        val KEY_AUTO_ATTACH_DIAGNOSTICS = booleanPreferencesKey("auto_attach_diagnostics")
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.fromString(prefs[KEY_THEME_MODE])
    }

    val performanceModeFlow: Flow<PerformanceMode> = context.dataStore.data.map { prefs ->
        PerformanceMode.fromString(prefs[KEY_PERFORMANCE_MODE])
    }

    val selectedIconFlow: Flow<AppIconOption> = context.dataStore.data.map { prefs ->
        AppIconOption.fromId(prefs[KEY_SELECTED_ICON])
    }

    val routerIpFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ROUTER_IP] ?: "192.168.1.1"
    }

    val routerWebUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ROUTER_WEB_URL] ?: "http://192.168.1.1/login.html"
    }

    val desktopModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DESKTOP_MODE] ?: false
    }

    val forceHighRefreshRateFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_FORCE_HIGH_REFRESH_RATE] ?: true
    }

    val targetRefreshRateFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_TARGET_REFRESH_RATE] ?: if (prefs[KEY_FORCE_HIGH_REFRESH_RATE] == false) 60 else 120
    }

    val telegramBotTokenFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_TELEGRAM_BOT_TOKEN] ?: ""
    }

    val telegramChatIdFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_TELEGRAM_CHAT_ID] ?: ""
    }

    val autoAttachDiagnosticsFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_ATTACH_DIAGNOSTICS] ?: true
    }

    suspend fun saveThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun savePerformanceMode(mode: PerformanceMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PERFORMANCE_MODE] = mode.name
        }
    }

    suspend fun saveSelectedIcon(icon: AppIconOption) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_ICON] = icon.id
        }
    }

    suspend fun saveRouterIp(ip: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ROUTER_IP] = ip
        }
    }

    suspend fun saveRouterWebUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ROUTER_WEB_URL] = url
        }
    }

    suspend fun saveDesktopMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DESKTOP_MODE] = enabled
        }
    }

    suspend fun saveForceHighRefreshRate(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FORCE_HIGH_REFRESH_RATE] = enabled
        }
    }

    suspend fun saveTargetRefreshRate(rateHz: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TARGET_REFRESH_RATE] = rateHz
            prefs[KEY_FORCE_HIGH_REFRESH_RATE] = (rateHz > 60)
        }
    }

    suspend fun saveTelegramCredentials(botToken: String, chatId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TELEGRAM_BOT_TOKEN] = botToken.trim()
            prefs[KEY_TELEGRAM_CHAT_ID] = chatId.trim()
        }
    }

    suspend fun saveAutoAttachDiagnostics(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_ATTACH_DIAGNOSTICS] = enabled
        }
    }
}
