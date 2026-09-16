package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.SoundManager
import com.example.billing.BillingManager

@Composable
fun PatronPassDialog(
    billingManager: BillingManager,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val soundManager = remember { SoundManager.getInstance(context) }
    val patronPrice by billingManager.patronPrice.collectAsState()
    val isUnlocked by billingManager.isPatronUnlocked.collectAsState()

    val goldColor = Color(0xFFFFD700)
    val goldDark = Color(0xFFC59B27)

    val woodBackground = if (isDarkTheme) {
        Brush.verticalGradient(
            listOf(Color(0xFF25160E), Color(0xFF180E08), Color(0xFF100804))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFF3D2314), Color(0xFF2A160C), Color(0xFF1B0D07))
        )
    }

    val outerStitchColor = if (isDarkTheme) {
        LeatherStitchDefaults.GoldStitch.copy(alpha = 0.85f)
    } else {
        Color(0xFFFFD54F).copy(alpha = 0.90f)
    }

    val animScale = remember { Animatable(0.82f) }
    LaunchedEffect(Unit) {
        soundManager.playButtonClick(0.75f)
        animScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = animScale.value
                    scaleY = animScale.value
                }
                .padding(horizontal = 4.dp, vertical = 12.dp)
                .background(Color.Black.copy(alpha = 0.60f), RoundedCornerShape(28.dp))
                .padding(bottom = 6.dp)
                .background(woodBackground, RoundedCornerShape(28.dp))
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(listOf(goldColor, goldDark, Color(0xFF7A5812))),
                    shape = RoundedCornerShape(28.dp)
                )
                .leatherStitchBorder(
                    color = outerStitchColor,
                    cornerRadius = 28.dp,
                    inset = 4.dp,
                    dashLength = 4.5.dp,
                    gapLength = 3.dp
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                Brush.radialGradient(listOf(Color(0xFF5A391F), Color(0xFF2B190D))),
                                CircleShape
                            )
                            .border(1.5.dp, Brush.linearGradient(listOf(goldColor, goldDark)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👑", fontSize = 19.sp)
                    }

                    Box(
                        modifier = Modifier
                            .background(goldColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, goldColor.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "★ PASE VIP ÚNICO ★",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = goldColor,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .bounceClick(onClick = onDismiss)
                            .background(Color(0xFF332014), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                    text = "PASE DEL PATRÓN",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = goldColor,
                    letterSpacing = 1.2.sp
                )

                Text(
                    text = "¡Conviértete en el mero dueño del rancho!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.90f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Benefits List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PatronBenefitRow(
                        emoji = "🛡️",
                        title = "Blindaje del Patrón (Segunda Oportunidad)",
                        description = "Salva tu partida de 1 dinamita por juego. ¡El mero dueño no cae a la primera!",
                        isDarkTheme = isDarkTheme
                    )
                    PatronBenefitRow(
                        emoji = "⚙️",
                        title = "Modo Personalizado Ilimitado",
                        description = "Crea tableros a tu medida con casillas y densidad a tu gusto.",
                        isDarkTheme = isDarkTheme
                    )
                    PatronBenefitRow(
                        emoji = "🤠",
                        title = "7 Distintivos Exclusivos VIP",
                        description = "Gallo Fino, Dinamita, Agave, Pistola, Bota, Campirano y Cuernos.",
                        isDarkTheme = isDarkTheme
                    )
                    PatronBenefitRow(
                        emoji = "👑",
                        title = "Insignia y Corona de Patrón",
                        description = "Luce tu distinción dorada en el menú y en tus victorias.",
                        isDarkTheme = isDarkTheme
                    )
                    PatronBenefitRow(
                        emoji = "✨",
                        title = "Experiencia 100% Sin Anuncios",
                        description = "Apoya el desarrollo independiente del rancho y juega sin interrupciones.",
                        isDarkTheme = isDarkTheme
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF1E461A), Color(0xFF2E6927), Color(0xFF1E461A))
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .border(1.5.dp, Color(0xFF81C784), RoundedCornerShape(16.dp))
                            .leatherStitchBorder(
                                color = Color(0xFFA5D6A7).copy(alpha = 0.70f),
                                cornerRadius = 16.dp,
                                inset = 2.5.dp
                            )
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "👑", fontSize = 21.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¡Pase del Patrón Activo • Eres el Mero Dueño!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Tactile VIP Unlock Button with Shimmer & Spring
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .bounceClick(onClick = {
                                activity?.let { billingManager.launchPurchaseFlow(it) }
                            })
                            .background(Color(0xFF7A4F0B), RoundedCornerShape(16.dp))
                            .padding(bottom = 4.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFFE082), Color(0xFFFFC107), Color(0xFFFFB300))
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.5.dp,
                                Brush.verticalGradient(listOf(Color(0xFFFFF9C4), Color(0xFFD4AF37))),
                                RoundedCornerShape(16.dp)
                            )
                            .leatherStitchBorder(
                                color = Color(0xFF523307).copy(alpha = 0.70f),
                                cornerRadius = 16.dp,
                                inset = 3.dp,
                                dashLength = 4.dp,
                                gapLength = 2.5.dp
                            )
                            .shimmerGoldenSweep(durationMillis = 2400)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "👑", fontSize = 21.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DESBLOQUEAR TODO • $patronPrice",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF261404),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Pago único de por vida. Sin suscripciones ni cargos ocultos.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.70f),
                        fontSize = 11.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Restore Purchases Button with bounce
                    Box(
                        modifier = Modifier
                            .bounceClick(onClick = {
                                billingManager.queryExistingPurchases()
                            })
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🔄 Restaurar Compras previas",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = goldColor.copy(alpha = 0.95f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatronBenefitRow(
    emoji: String,
    title: String,
    description: String,
    isDarkTheme: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        if (isDarkTheme) Color(0xFF331F14) else Color(0xFF402618),
                        if (isDarkTheme) Color(0xFF24150D) else Color(0xFF301B11)
                    )
                ),
                RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFF8C6424), Color(0xFF4A3018))
                ),
                RoundedCornerShape(14.dp)
            )
            .leatherStitchBorder(
                color = Color(0xFFFFD54F).copy(alpha = 0.35f),
                cornerRadius = 14.dp,
                inset = 2.dp,
                dashLength = 3.dp,
                gapLength = 2.5.dp
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF54341B), Color(0xFF26160C))),
                    CircleShape
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFD700).copy(alpha = 0.75f), Color(0xFF8C6414))
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 19.sp)
        }

        Spacer(modifier = Modifier.width(11.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFE082)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )
        }
    }
}

