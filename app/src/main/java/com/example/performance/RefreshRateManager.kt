package com.example.performance

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.WindowManager
import androidx.compose.runtime.Immutable

@Immutable
data class DisplayRefreshInfo(
    val currentRefreshRate: Float,
    val maxRefreshRate: Float,
    val supportedRates: List<Float>,
    val displayModeName: String,
    val isHighRefreshRateActive: Boolean
)

object RefreshRateManager {

    /**
     * Inspects the display and detects all supported refresh rate modes (60Hz, 90Hz, 120Hz, 144Hz, 165Hz).
     */
    fun getDisplayRefreshInfo(context: Context): DisplayRefreshInfo {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)

        val currentRate = display?.refreshRate ?: 60f
        val supportedRates = mutableSetOf<Float>()
        var maxRate = currentRate

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && display != null) {
            val modes = display.supportedModes
            for (mode in modes) {
                val rate = Math.round(mode.refreshRate * 10f) / 10f
                supportedRates.add(rate)
                if (rate > maxRate) {
                    maxRate = rate
                }
            }
        } else {
            supportedRates.add(currentRate)
        }

        val sortedRates = supportedRates.sortedDescending()
        val modeName = when {
            maxRate >= 165f -> "Ultra 165Hz (Gaming/Flagship Pad)"
            maxRate >= 144f -> "Pro 144Hz (Smooth Display)"
            maxRate >= 120f -> "Extreme 120Hz Fluid Motion"
            maxRate >= 90f -> "Smooth 90Hz Display"
            else -> "Standard 60Hz Display"
        }

        return DisplayRefreshInfo(
            currentRefreshRate = currentRate,
            maxRefreshRate = maxRate,
            supportedRates = sortedRates,
            displayModeName = modeName,
            isHighRefreshRateActive = currentRate > 65f
        )
    }

    /**
     * Applies target refresh rate (60Hz, 120Hz, 144Hz) smoothly to window.
     */
    fun applyTargetRefreshRate(activity: Activity, targetRateHz: Int) {
        try {
            val window = activity.window
            // Ensure hardware acceleration flag is active
            window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    activity.display
                } else {
                    @Suppress("DEPRECATION")
                    window.windowManager.defaultDisplay
                } ?: return

                val modes = display.supportedModes
                if (modes.isNotEmpty()) {
                    val targetMode = when (targetRateHz) {
                        60 -> modes.find { it.refreshRate in 59.0f..61.0f } ?: modes.minByOrNull { it.refreshRate }
                        120 -> modes.find { it.refreshRate in 118.0f..122.0f }
                            ?: modes.filter { it.refreshRate in 110.0f..130.0f }.minByOrNull { Math.abs(it.refreshRate - 120f) }
                            ?: modes.maxByOrNull { it.refreshRate }
                        144 -> modes.find { it.refreshRate in 140.0f..146.0f }
                            ?: modes.filter { it.refreshRate >= 140.0f }.minByOrNull { it.refreshRate }
                            ?: modes.maxByOrNull { it.refreshRate }
                        else -> modes.minByOrNull { Math.abs(it.refreshRate - targetRateHz) }
                    }

                    targetMode?.let { mode ->
                        val params = window.attributes
                        params.preferredDisplayModeId = mode.modeId
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            params.preferredRefreshRate = targetRateHz.toFloat()
                        }
                        window.attributes = params
                    }
                }
            }

            // Android 11+ FrameRate hint
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val params = window.attributes
                params.preferredRefreshRate = targetRateHz.toFloat()
                window.attributes = params
            }
        } catch (_: Exception) {
            // Gracefully ignore devices that restrict display mode switching
        }
    }

    /**
     * Backward-compatible helper requesting standard or high refresh rate.
     */
    fun applyRefreshRatePreference(activity: Activity, forceHighRefreshRate: Boolean = true) {
        applyTargetRefreshRate(activity, if (forceHighRefreshRate) 120 else 60)
    }
}
