package com.example.performance

enum class PerformanceMode(
    val label: String,
    val description: String,
    val pollingIntervalMs: Long
) {
    LOW(
        label = "Low",
        description = "Power saving mode. Throttles background polling (4s) and conserves battery.",
        pollingIntervalMs = 4000L
    ),
    MEDIUM(
        label = "Medium",
        description = "Default balanced mode. Smooth animations with 2s live polling.",
        pollingIntervalMs = 2000L
    ),
    BOOST(
        label = "Boost",
        description = "Peak performance mode. 1s live polling, prioritizes highest refresh rate.",
        pollingIntervalMs = 1000L
    );

    companion object {
        fun fromString(value: String?): PerformanceMode {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}
