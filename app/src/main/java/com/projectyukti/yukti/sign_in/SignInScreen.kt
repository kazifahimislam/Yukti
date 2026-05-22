package com.projectyukti.yukti.sign_in

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

// ── Color tokens ──────────────────────────────────────────
private val BgDeep       = Color(0xFF0D1117)
private val BgCard       = Color(0xFF1C2433)
private val BgButton     = Color(0xFF21262D)
private val BorderSubtle = Color(0xFF30363D)
private val TealPrimary  = Color(0xFF2DD4BF)
private val TealDim      = Color(0xFF14B8A6)
private val TealGlow     = Color(0x1A2DD4BF)
private val BlueSky      = Color(0xFF0EA5E9)
private val TextPrimary  = Color(0xFFE6EDF3)
private val TextMuted    = Color(0xFF8B949E)
private val TextFaint    = Color(0xFF484F58)
// ─────────────────────────────────────────────────────────

@Composable
fun SignInScreen(
    state: SignInState,
    onSignInClick: () -> Unit,
    navController: NavHostController
) {
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // ── Entrance animation ──────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); visible = true }

    // ── Show error toast and reset loading ──────────────
    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            isLoading = false
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    // ── Button pulse animation while loading ────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // ── Ambient glow bob — uses offset() NOT padding() ──
    // FIX: padding() crashes on negative values; offset() handles negatives fine
    val glowOffset by infiniteTransition.animateFloat(
        initialValue = -16f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowBob"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .windowInsetsPadding(WindowInsets.systemBars),
        contentAlignment = Alignment.Center
    ) {

        // ── Ambient background glow ──────────────────────
        // Using .offset() instead of .padding() — safe with negative values
        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.Center)
                .offset(y = glowOffset.dp)   // ✅ offset() allows negative, padding() does not
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(TealGlow, Color.Transparent)
                    ),
                    shape = CircleShape
                )
                .blur(56.dp)
        )

        // ── Entrance slide-fade ───────────────────────────
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + slideInVertically(
                initialOffsetY = { it / 10 },
                animationSpec = tween(400, easing = EaseOut)
            )
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── App icon badge ───────────────────────────
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(TealDim, BlueSky),
                                start = Offset(0f, 0f),
                                end = Offset(72f, 72f)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Inventory2,
                        contentDescription = "Yukti",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Yukti",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "AI-powered inventory management",
                    fontSize = 14.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(36.dp))

                // ── Card ─────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = BgCard,
                    border = BorderStroke(1.dp, BorderSubtle),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Welcome back",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Sign in to continue to your workspace",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(28.dp))

                        // ── Sign-In Button ───────────────────────
                        Button(
                            onClick = {
                                isLoading = true
                                onSignInClick()
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .alpha(if (isLoading) pulseAlpha else 1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BgButton,
                                contentColor = TextPrimary,
                                disabledContainerColor = BgButton,
                                disabledContentColor = TextMuted
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isLoading) TealPrimary.copy(alpha = 0.45f)
                                else BorderSubtle
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            )
                        ) {
                            AnimatedContent(
                                targetState = isLoading,
                                transitionSpec = {
                                    fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                                },
                                label = "btnContent"
                            ) { loading ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = TealPrimary
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = "Signing in...",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextMuted
                                        )
                                    } else {
                                        // TODO: Replace with Image(painterResource(R.drawable.ic_google), ...)
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "G",
                                                color = Color(0xFF4285F4),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = "Continue with Google",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── Divider ──────────────────────────────
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = BorderSubtle
                            )
                            Text(
                                text = "  Secure sign-in  ",
                                fontSize = 11.sp,
                                color = TextFaint
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = BorderSubtle
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── Feature pills ────────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("🤖 AI sorting", "📦 Real-time", "🔒 Secure")
                                .forEach { label ->
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(50),
                                        color = BgCard,
                                        border = BorderStroke(1.dp, BorderSubtle)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            color = TextMuted,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            modifier = Modifier.padding(
                                                horizontal = 4.dp,
                                                vertical = 6.dp
                                            )
                                        )
                                    }
                                }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Footer ───────────────────────────────────
                Text(
                    text = "By continuing you agree to our Terms & Privacy Policy",
                    fontSize = 11.sp,
                    color = TextFaint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}
