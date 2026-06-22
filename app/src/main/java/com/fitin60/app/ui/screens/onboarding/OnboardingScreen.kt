package com.fitin60.app.ui.screens.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF080F1E), Color(0xFF0B1F38)),
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
        ) {
            Spacer(Modifier.weight(1.2f))

            SphereBadge(size = 192.dp)

            Spacer(Modifier.height(32.dp))

            Text(
                text = "FITIN60",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Your transformation. Your plan.",
                color = Color.White.copy(alpha = 0.55f),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
            )

            Spacer(Modifier.weight(1.5f))

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22D3EE),
                    contentColor = Color(0xFF080F1E),
                ),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                Text(
                    text = "Import your 60-day plan",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    letterSpacing = 0.3.sp,
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Paste JSON from ChatGPT or Claude to get started.",
                color = Color.White.copy(alpha = 0.38f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SphereBadge(size: Dp) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = this.size.minDimension / 2f
            val cx = center.x
            val cy = center.y

            // Cast shadow (oval beneath sphere)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x66000000), Color.Transparent),
                    center = Offset(cx, cy + r * 0.9f),
                    radius = r * 0.75f,
                ),
                topLeft = Offset(cx - r * 0.75f, cy + r * 0.55f),
                size = Size(r * 1.5f, r * 0.55f),
            )

            // Main sphere: radial gradient from upper-left (bright cyan) to lower-right (deep navy)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF67E8F9),
                        Color(0xFF0EA5E9),
                        Color(0xFF075985),
                        Color(0xFF0A1628),
                    ),
                    center = Offset(cx - r * 0.28f, cy - r * 0.32f),
                    radius = r * 1.7f,
                ),
                radius = r,
                center = Offset(cx, cy),
            )

            // Specular highlight — top-left bright spot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xCCFFFFFF), Color(0x44FFFFFF), Color.Transparent),
                    center = Offset(cx - r * 0.4f, cy - r * 0.42f),
                    radius = r * 0.42f,
                ),
                radius = r * 0.42f,
                center = Offset(cx - r * 0.4f, cy - r * 0.42f),
            )

            // Secondary soft glow on left rim
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3367E8F9), Color.Transparent),
                    center = Offset(cx - r * 0.7f, cy),
                    radius = r * 0.45f,
                ),
                radius = r * 0.45f,
                center = Offset(cx - r * 0.7f, cy),
            )

            // Bottom-right ambient occlusion darkening
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0x55000020)),
                    center = Offset(cx + r * 0.38f, cy + r * 0.42f),
                    radius = r * 0.68f,
                ),
                radius = r * 0.68f,
                center = Offset(cx + r * 0.38f, cy + r * 0.42f),
            )

            // Outer teal rim glow
            drawCircle(
                color = Color(0x6622D3EE),
                radius = r - 1.5f,
                center = Offset(cx, cy),
                style = Stroke(width = 3f),
            )

            // Inner thin rim
            drawCircle(
                color = Color(0x3399FFFF),
                radius = r - 5f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f),
            )

            // Lightning bolt — white, centered
            val bh = r * 1.05f
            val bw = r * 0.52f
            val bl = cx - bw * 0.32f
            val bt = cy - bh * 0.5f

            val bolt = Path().apply {
                moveTo(bl + bw * 0.68f, bt)
                lineTo(bl, bt + bh * 0.49f)
                lineTo(bl + bw * 0.46f, bt + bh * 0.49f)
                lineTo(bl + bw * 0.12f, bt + bh)
                lineTo(bl + bw, bt + bh * 0.53f)
                lineTo(bl + bw * 0.54f, bt + bh * 0.53f)
                close()
            }
            // Bolt shadow for depth
            val boltShadow = Path().apply {
                moveTo(bl + bw * 0.68f + 2f, bt + 3f)
                lineTo(bl + 2f, bt + bh * 0.49f + 3f)
                lineTo(bl + bw * 0.46f + 2f, bt + bh * 0.49f + 3f)
                lineTo(bl + bw * 0.12f + 2f, bt + bh + 3f)
                lineTo(bl + bw + 2f, bt + bh * 0.53f + 3f)
                lineTo(bl + bw * 0.54f + 2f, bt + bh * 0.53f + 3f)
                close()
            }
            drawPath(boltShadow, Color(0x33000020))
            drawPath(bolt, Color.White)
        }
    }
}
