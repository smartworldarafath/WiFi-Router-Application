package com.example.performance

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.R

enum class AppIconOption(
    val id: String,
    val title: String,
    val aliasName: String,
    @DrawableRes val previewResId: Int,
    val primaryColor: Color,
    val accentColor: Color,
    val description: String
) {
    DEFAULT(
        id = "icon_1",
        title = "Icon 1 (Default)",
        aliasName = "com.example.MainActivityDefault",
        previewResId = R.drawable.ic_custom_icon_1,
        primaryColor = Color(0xFF005BAC),
        accentColor = Color(0xFF00C2FF),
        description = "Official Netis Electric Blue & Wave Crest (Default)"
    ),
    CYBER(
        id = "icon_2",
        title = "Icon 2 (Cyber)",
        aliasName = "com.example.MainActivityCyber",
        previewResId = R.drawable.ic_custom_icon_2,
        primaryColor = Color(0xFF0A0E17),
        accentColor = Color(0xFF00FFCC),
        description = "Cyberpunk Emerald Router Chassis"
    ),
    NEON(
        id = "icon_3",
        title = "Icon 3 (Neon)",
        aliasName = "com.example.MainActivityNeon",
        previewResId = R.drawable.ic_custom_icon_3,
        primaryColor = Color(0xFF1E0E38),
        accentColor = Color(0xFFFF007F),
        description = "Neon Violet Glow & Quantum Pulse"
    ),
    DARK(
        id = "icon_4",
        title = "Icon 4 (Midnight)",
        aliasName = "com.example.MainActivityDark",
        previewResId = R.drawable.ic_custom_icon_4,
        primaryColor = Color(0xFF18181B),
        accentColor = Color(0xFF38BDF8),
        description = "Midnight Stealth Minimalist Router"
    ),
    VIBRANT(
        id = "icon_5",
        title = "Icon 5 (Solar)",
        aliasName = "com.example.MainActivityVibrant",
        previewResId = R.drawable.ic_custom_icon_5,
        primaryColor = Color(0xFFFF5722),
        accentColor = Color(0xFFFFD600),
        description = "Solar Amber Flame & Radiating Waves"
    );

    companion object {
        fun fromId(id: String?): AppIconOption {
            return entries.find { 
                it.id.equals(id, ignoreCase = true) || 
                (id == "default" && it == DEFAULT) ||
                (id == "cyber" && it == CYBER) ||
                (id == "neon" && it == NEON) ||
                (id == "dark" && it == DARK) ||
                (id == "vibrant" && it == VIBRANT)
            } ?: DEFAULT
        }
    }
}

object IconSwitchManager {

    fun setAppIcon(context: Context, targetIcon: AppIconOption): Boolean {
        return try {
            val pm = context.packageManager
            val packageName = context.packageName

            // Enable selected alias and disable others
            for (icon in AppIconOption.entries) {
                val componentName = ComponentName(packageName, icon.aliasName)
                val newState = if (icon == targetIcon) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }

                pm.setComponentEnabledSetting(
                    componentName,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getActiveIcon(context: Context): AppIconOption {
        return try {
            val pm = context.packageManager
            val packageName = context.packageName

            for (icon in AppIconOption.entries) {
                val componentName = ComponentName(packageName, icon.aliasName)
                val state = pm.getComponentEnabledSetting(componentName)
                if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                    return icon
                }
            }
            AppIconOption.DEFAULT
        } catch (_: Exception) {
            AppIconOption.DEFAULT
        }
    }
}
