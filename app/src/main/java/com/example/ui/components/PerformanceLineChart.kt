package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.performance.PerformanceTelemetry

/**
 * Backward compatibility alias: redirects to the redesigned WaterWavePerformanceGraph.
 */
@Composable
fun PerformanceLineChart(
    telemetry: PerformanceTelemetry,
    modifier: Modifier = Modifier
) {
    WaterWavePerformanceGraph(telemetry = telemetry, modifier = modifier)
}
