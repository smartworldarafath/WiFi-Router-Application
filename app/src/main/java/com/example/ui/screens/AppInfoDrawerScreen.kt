package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.performance.AppIconOption
import com.example.ui.theme.NetisBluePrimary
import com.example.ui.theme.NetisCyanAccent
import com.example.update.GitHubReleaseService

enum class PaymentMethodOption(
    val title: String,
    val subtitle: String,
    val brandColor: Color,
    val accountIdentifier: String,
    val copyValue: String,
    @DrawableRes val qrDrawableRes: Int?,
    val qrSeed: Int = 101
) {
    REDOT_PAY(
        title = "RedotPay",
        subtitle = "Scan with RedotPay App • Global Crypto / Card",
        brandColor = Color(0xFFE51D24),
        accountIdentifier = "RedotPay ID: 1965421414",
        copyValue = "1965421414",
        qrDrawableRes = com.example.R.drawable.qr_redotpay
    ),
    NSAVE(
        title = "nsave",
        subtitle = "Md Arafath Rahman (@arafath_rahman9) • Zero-Fee Friends & Family",
        brandColor = Color(0xFF1E3A8A),
        accountIdentifier = "@arafath_rahman9",
        copyValue = "@arafath_rahman9",
        qrDrawableRes = com.example.R.drawable.qr_nsave
    ),
    NAGAD(
        title = "Nagad",
        subtitle = "Bangladesh Mobile Banking / Personal",
        brandColor = Color(0xFFF26522),
        accountIdentifier = "Nagad Personal: 017XXXXXXXX",
        copyValue = "017XXXXXXXX",
        qrDrawableRes = null,
        qrSeed = 101
    )
}

@Composable
fun AppInfoDrawerScreen(
    currentIcon: AppIconOption = AppIconOption.DEFAULT,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showCoffeePopup by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_info_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // App Identity Card - Redesigned consistent with Update section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App Icon with consistent 64.dp sizing and exact original proportions (never zoomed out)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(currentIcon.primaryColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = currentIcon.previewResId),
                            contentDescription = "App Icon",
                            modifier = Modifier
                                .size(52.dp)
                                .aspectRatio(1f),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "App Version",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            // Clean version pill badge next to app version
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = GitHubReleaseService.CURRENT_APP_VERSION,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "WiFi Router App",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${GitHubReleaseService.CURRENT_APP_VERSION} (Build 4) • Companion App",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Developer Information Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Developer Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Developer Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Developer Name",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Arafath",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Developer Email
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Email Address",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "arafathrahman711@gmail.com",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:arafathrahman711@gmail.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "WiFi Router App Feedback")
                                }
                                try {
                                    context.startActivity(emailIntent)
                                } catch (_: Exception) {
                                    clipboardManager.setText(AnnotatedString("arafathrahman711@gmail.com"))
                                    Toast.makeText(context, "Email copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Contact")
                        }
                    }
                }
            }
        }

        // Redesigned "Buy Me a Coffee" Section - Shows ONLY "Buy Me a Coffee"
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { showCoffeePopup = true }
                    .testTag("buy_me_a_coffee_button"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF161E2E)
                ),
                border = BorderStroke(1.dp, Color(0xFF2C3E55))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFFB300),
                                            Color(0xFFFF8F00)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Coffee,
                                contentDescription = "Buy Me a Coffee",
                                tint = Color.Black,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Buy Me a Coffee",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Support project development & future updates",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFA0B2C6),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF222F44))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Support",
                                color = Color(0xFFFFB300),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Smooth Animated Card-Style Mini Popup for Payment Methods
    if (showCoffeePopup) {
        BuyMeACoffeeDialog(
            onDismiss = { showCoffeePopup = false }
        )
    }
}

/**
 * Animated Card-Style Mini Popup Window for Buy Me a Coffee.
 * Hardware-accelerated transitions ensure zero frame drops while displaying payment methods cleanly.
 */
@Composable
private fun BuyMeACoffeeDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf(PaymentMethodOption.REDOT_PAY) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(initialScale = 0.90f, animationSpec = spring(stiffness = 500f)) + fadeIn(),
            exit = scaleOut(targetScale = 0.90f) + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFF2B3A52), RoundedCornerShape(24.dp))
                    .testTag("buy_me_a_coffee_popup"),
                color = Color(0xFF0F1522),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header with Coffee icon and close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFB300).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Coffee,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Buy Me a Coffee",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Select a payment method to support",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF869AB5),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B2433))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFFA0B2C9),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Payment Method Dock Selector: RedotPay | nsave | Nagad
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF141C2A))
                            .border(1.dp, Color(0xFF222C3E), RoundedCornerShape(14.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PaymentMethodOption.entries.forEach { method ->
                            val isSelected = selectedMethod == method
                            val pillShape = RoundedCornerShape(10.dp)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(pillShape)
                                    .background(
                                        if (isSelected) {
                                            Brush.horizontalGradient(
                                                listOf(
                                                    method.brandColor,
                                                    method.brandColor.copy(alpha = 0.85f)
                                                )
                                            )
                                        } else {
                                            Brush.horizontalGradient(
                                                listOf(Color.Transparent, Color.Transparent)
                                            )
                                        }
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        selectedMethod = method
                                    }
                                    .padding(vertical = 8.dp)
                                    .testTag("method_${method.title.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = method.title,
                                    color = if (isSelected) Color.White else Color(0xFF8899AE),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Detailed Card for Selected Payment Method
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF151D2C)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF243144))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = selectedMethod.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFA0B4CC),
                                fontSize = 11.5.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // QR Visual
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF090E17))
                                    .padding(vertical = 14.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (selectedMethod.qrDrawableRes != null) {
                                        Image(
                                            painter = painterResource(id = selectedMethod.qrDrawableRes!!),
                                            contentDescription = "${selectedMethod.title} QR",
                                            modifier = Modifier
                                                .size(150.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White)
                                                .padding(6.dp)
                                        )
                                    } else {
                                        StylizedQrVisual(
                                            primaryColor = selectedMethod.brandColor,
                                            seed = selectedMethod.qrSeed,
                                            modifier = Modifier.size(150.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = selectedMethod.accountIdentifier,
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = NetisCyanAccent,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText(selectedMethod.title, selectedMethod.copyValue))
                                                    Toast.makeText(context, "${selectedMethod.title} details copied", Toast.LENGTH_SHORT).show()
                                                }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Buttons: Copy, Save, Share
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText(selectedMethod.title, selectedMethod.copyValue))
                                        Toast.makeText(context, "Copied: ${selectedMethod.copyValue}", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 11.5.sp)
                                }

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "${selectedMethod.title} QR saved to gallery", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = selectedMethod.brandColor)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save", fontSize = 11.5.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Support WiFi Router App via ${selectedMethod.title}: ${selectedMethod.copyValue}"
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share ${selectedMethod.title} Info"))
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share", fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StylizedQrVisual(
    primaryColor: Color,
    seed: Int,
    modifier: Modifier = Modifier
) {
    val matrixSize = 15
    val activeCells = remember(seed) {
        val list = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until matrixSize) {
            for (col in 0 until matrixSize) {
                val inTopLeft = row < 5 && col < 5
                val inTopRight = row < 5 && col >= matrixSize - 5
                val inBottomLeft = row >= matrixSize - 5 && col < 5
                val inCenter = row in 6..8 && col in 6..8
                if (!inTopLeft && !inTopRight && !inBottomLeft && !inCenter) {
                    val pseudoRandom = ((row * 31 + col * 17 + seed) % 5) == 0 ||
                            ((row * 7 + col * 23 + seed) % 3) == 0
                    if (pseudoRandom) {
                        list.add(Pair(col, row))
                    }
                }
            }
        }
        list
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cellSize = w / matrixSize.toFloat()

        // Background card
        drawRoundRect(
            color = Color.White,
            size = size,
            cornerRadius = CornerRadius(14.dp.toPx())
        )

        // Draw Corner Finder Patterns (Top-Left, Top-Right, Bottom-Left)
        val finderIndices = listOf(
            Offset(0f, 0f),
            Offset((matrixSize - 4) * cellSize, 0f),
            Offset(0f, (matrixSize - 4) * cellSize)
        )

        for (pos in finderIndices) {
            // Outer square
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(pos.x + 4f, pos.y + 4f),
                size = Size(cellSize * 3.6f, cellSize * 3.6f),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            // Inner white ring
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(pos.x + 4f + cellSize * 0.7f, pos.y + 4f + cellSize * 0.7f),
                size = Size(cellSize * 2.2f, cellSize * 2.2f),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            // Center solid block
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(pos.x + 4f + cellSize * 1.2f, pos.y + 4f + cellSize * 1.2f),
                size = Size(cellSize * 1.2f, cellSize * 1.2f),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
        }

        // Procedural matrix pixels from precomputed list
        for (cell in activeCells) {
            drawRoundRect(
                color = primaryColor.copy(alpha = 0.85f),
                topLeft = Offset(cell.first * cellSize + 2f, cell.second * cellSize + 2f),
                size = Size(cellSize - 4f, cellSize - 4f),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
        }

        // Center emblem
        drawCircle(
            color = primaryColor,
            radius = cellSize * 1.6f,
            center = Offset(w / 2f, h / 2f)
        )
        drawCircle(
            color = Color.White,
            radius = cellSize * 1.2f,
            center = Offset(w / 2f, h / 2f)
        )
    }
}
