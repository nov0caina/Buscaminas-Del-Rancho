package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.billing.BillingManager

@Composable
fun PatronPassDialog(
    billingManager: BillingManager,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val patronPrice by billingManager.patronPrice.collectAsState()
    val isUnlocked by billingManager.isPatronUnlocked.collectAsState()

    val goldColor = Color(0xFFFFD700)
    val goldDark = Color(0xFFC59B27)
    val woodDark = if (isDarkTheme) Color(0xFF1E140E) else Color(0xFF2C1E16)

    val animScale = remember { Animatable(0.82f) }
    LaunchedEffect(Unit) {
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
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                .padding(bottom = 6.dp)
                .background(woodDark, RoundedCornerShape(28.dp))
                .border(2.dp, Brush.verticalGradient(listOf(goldColor, goldDark, Color(0xFF7A5812))), RoundedCornerShape(28.dp))
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
                            .size(36.dp)
                            .background(Color(0xFF3D2A1D), CircleShape)
                            .border(1.dp, goldColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👑", fontSize = 18.sp)
                    }

                    Box(
                        modifier = Modifier
                            .background(goldColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, goldColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "★ PASE VIP ÚNICO ★",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = goldColor
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = "PASE DEL PATRÓN",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = goldColor,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "¡Conviértete en el mero dueño del rancho!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Benefits List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PatronBenefitRow(
                        emoji = "💣",
                        title = "Modo Belikón (99 Minas)",
                        description = "El reto supremo para los más bravos de la sierra."
                    )
                    PatronBenefitRow(
                        emoji = "📐",
                        title = "Modo Personalizado Libre",
                        description = "Crea tableros a tu medida con casillas y densidad a tu gusto."
                    )
                    PatronBenefitRow(
                        emoji = "🔫",
                        title = "7 Distintivos Exclusivos VIP",
                        description = "Gallo Fino, Dinamita, Agave, Pistola, Bota, Campirano y Cuernos."
                    )
                    PatronBenefitRow(
                        emoji = "✨",
                        title = "Insignia Dorada de Patrón",
                        description = "Luce tu corona en el menú y apoya el juego 100% libre de anuncios."
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2E5A27), RoundedCornerShape(16.dp))
                            .border(1.5.dp, Color(0xFF66BB6A), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👑 ¡Pase del Patrón Activo!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    // Unlock Button
                    Button(
                        onClick = {
                            activity?.let { billingManager.launchPurchaseFlow(it) }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = goldColor,
                            contentColor = Color(0xFF2A1708)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "👑", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DESBLOQUEAR TODO • $patronPrice",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Pago único de por vida. Sin suscripciones ni cargos ocultos.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Restore Purchases Button
                    TextButton(
                        onClick = {
                            billingManager.queryExistingPurchases()
                        }
                    ) {
                        Text(
                            text = "🔄 Restaurar Compras previas",
                            style = MaterialTheme.typography.labelMedium,
                            color = goldColor.copy(alpha = 0.9f)
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
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF332219), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF5A3C2A), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF4A3122), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(10.dp))

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
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp
            )
        }
    }
}
