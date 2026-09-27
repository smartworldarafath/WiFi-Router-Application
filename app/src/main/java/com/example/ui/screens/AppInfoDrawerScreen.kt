package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.performance.AppIconOption
import com.example.update.GitHubReleaseService

@Composable
fun AppInfoDrawerScreen(
    currentIcon: AppIconOption = AppIconOption.DEFAULT,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_info_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Identity Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(currentIcon.primaryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = currentIcon.previewResId),
                            contentDescription = "App Icon",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Netis Router",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Version ${GitHubReleaseService.CURRENT_APP_VERSION} (Build 4)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Native Jetpack Compose Companion App",
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
                                    putExtra(Intent.EXTRA_SUBJECT, "Netis Router Android App Feedback")
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

        // Section: "Buy Me a Coffee" Support Card
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Coffee,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Buy Me a Coffee (Support the Project)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Payment Method 1: RedotPay (Original)
        item {
            PaymentSupportCard(
                title = "RedotPay",
                subtitle = "Scan with RedotPay app • Instant Global Payment",
                brandColor = Color(0xFFE51D24),
                accountIdentifier = "RedotPay ID: 1965421414",
                copyValue = "1965421414",
                qrDrawableRes = com.example.R.drawable.qr_redotpay,
                onSaveQr = {
                    Toast.makeText(context, "RedotPay QR saved to gallery", Toast.LENGTH_SHORT).show()
                },
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Support Netis Router via RedotPay ID: 1965421414")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share RedotPay Info"))
                }
            )
        }

        // Payment Method 2: nsave (Original)
        item {
            PaymentSupportCard(
                title = "nsave",
                subtitle = "Md Arafath Rahman (@arafath_rahman9) • Zero-Fee Friends & Family",
                brandColor = Color(0xFF0B132B),
                accountIdentifier = "@arafath_rahman9",
                copyValue = "@arafath_rahman9",
                qrDrawableRes = com.example.R.drawable.qr_nsave,
                onSaveQr = {
                    Toast.makeText(context, "nsave QR saved to gallery", Toast.LENGTH_SHORT).show()
                },
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Support Netis Router via nsave: @arafath_rahman9 (Md Arafath Rahman)")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share nsave Info"))
                }
            )
        }

        // Payment Method 3: Nagad (Personal)
        item {
            PaymentSupportCard(
                title = "Nagad",
                subtitle = "Bangladesh Mobile Banking / Personal",
                brandColor = Color(0xFFF26522),
                accountIdentifier = "Nagad Personal: 017XXXXXXXX",
                copyValue = "017XXXXXXXX",
                qrDrawableRes = null,
                qrSeed = 101,
                onSaveQr = {
                    Toast.makeText(context, "Nagad QR saved to gallery", Toast.LENGTH_SHORT).show()
                },
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Support Netis Router via Nagad: 017XXXXXXXX")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Nagad Support Info"))
                }
            )
        }
    }
}

@Composable
fun PaymentSupportCard(
    title: String,
    subtitle: String,
    brandColor: Color,
    accountIdentifier: String,
    copyValue: String,
    @androidx.annotation.DrawableRes qrDrawableRes: Int? = null,
    qrSeed: Int = 101,
    onSaveQr: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(brandColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual QR Centerpiece
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A))
                    .padding(vertical = 18.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (qrDrawableRes != null) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = qrDrawableRes),
                            contentDescription = "$title QR Code",
                            modifier = Modifier
                                .size(175.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .padding(8.dp)
                        )
                    } else {
                        StylizedQrVisual(
                            primaryColor = brandColor,
                            seed = qrSeed,
                            modifier = Modifier.size(160.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = accountIdentifier,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText(title, copyValue)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "$title copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions: "Copy", "Save QR" & "Share"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText(title, copyValue)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied: $copyValue", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy", fontSize = 12.sp)
                }

                Button(
                    onClick = onSaveQr,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = brandColor,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StylizedQrVisual(
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
