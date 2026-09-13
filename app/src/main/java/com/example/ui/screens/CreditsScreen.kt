package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.particles.DustParticleSystem
import com.example.ui.particles.RanchoSmokeParticleSystem
import kotlin.math.PI
import kotlin.math.sin

/**
 * Tipo de enlace social o portafolio.
 */
enum class SocialLinkType(val defaultLabel: String, val emoji: String) {
    GITHUB("GitHub", "🐙"),
    INSTAGRAM("Instagram", "📸"),
    TWITTER("X / Twitter", "🐦"),
    MUSIC("Música", "🎵"),
    WEB("Sitio Web", "🌐")
}

/**
 * Modelo de datos para un integrante de los créditos.
 */
data class Contributor(
    val name: String,
    val role: String,
    val badgeEmoji: String,
    val description: String,
    val linkUrl: String? = null,
    val linkType: SocialLinkType? = null,
    val linkCustomLabel: String? = null,
    val roleColor: Color = Color(0xFFFFB300) // Default Gold
)

@Composable
fun CreditsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val dustParticleSystem = remember { DustParticleSystem(150) }
    val smokeParticleSystem = remember { RanchoSmokeParticleSystem(150) }

    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransitionCredits")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "credits_particles"
    )

    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val contributors = remember {
        listOf(
            Contributor(
                name = "nov0caina",
                role = "Desarrollador",
                badgeEmoji = "👨‍💻",
                description = "Código y Diseño",
                linkUrl = "https://github.com/nov0caina",
                linkType = SocialLinkType.GITHUB,
                linkCustomLabel = "github.com/nov0caina",
                roleColor = Color(0xFFFFB300) // Oro
            ),
            Contributor(
                name = "Dascheika",
                role = "Advisor",
                badgeEmoji = "🧠",
                description = "Asesoría estratégica, dirección creativa y balance general del proyecto.",
                linkUrl = null,
                linkType = SocialLinkType.WEB,
                roleColor = Color(0xFF64B5F6) // Azul claro
            ),
            Contributor(
                name = "Nazza Urias",
                role = "Beta Tester",
                badgeEmoji = "🎯",
                description = "Pruebas de jugabilidad, detección de fallos y optimización de controles táctiles.",
                linkUrl = null,
                linkType = SocialLinkType.WEB,
                roleColor = Color(0xFF81C784) // Verde
            ),
            Contributor(
                name = "Jordan Ramirez",
                role = "Beta Tester",
                badgeEmoji = "🤠",
                description = "Retroalimentación en dificultades, experiencia de usuario y calibración del rancho.",
                linkUrl = null,
                linkType = SocialLinkType.WEB,
                roleColor = Color(0xFFFF8A65) // Terracota cálido
            ),
            Contributor(
                name = "Jem The Prod",
                role = "Música (Libre Uso)",
                badgeEmoji = "🎺",
                description = "Pistas musicales de banda sinaloense y corridos utilizadas bajo licencia de libre uso con atribución.",
                linkUrl = null,
                linkType = SocialLinkType.MUSIC,
                roleColor = Color(0xFFBA68C8) // Púrpura belikon
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Dynamic Particle Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = (sin(progress * 2 * PI) * 20f).toFloat()
                    val scale = 1f + (sin(progress * PI) * 0.05f).toFloat()
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            if (isDarkTheme) {
                smokeParticleSystem.setupRanchoSmoke(size.width / 2f, size.height / 2f, size.width * 0.45f)
                smokeParticleSystem.render(this, progress, size.width, size.height)
            } else {
                dustParticleSystem.setupAmbientDust(size.width, size.height)
                dustParticleSystem.render(this, progress)
            }
        }

        // Main Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 650.dp)
                .align(Alignment.TopCenter)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header Row with 3D Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 3D Back Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .bounceClick(onClick = onBack)
                        .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(bottom = 5.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .leatherStitchBorder(
                            color = if (isDarkTheme) LeatherStitchDefaults.DarkThemeStitch.copy(alpha = 0.60f)
                            else LeatherStitchDefaults.DayThemeStitch.copy(alpha = 0.75f),
                            cornerRadius = 12.dp,
                            inset = 2.5.dp,
                            dashLength = 3.5.dp,
                            gapLength = 2.5.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Créditos 🤠",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(2f, 4f),
                                blurRadius = 6f
                            )
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "El equipo detrás del rancho",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lazy List of Credits Cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Banner Card: Buscaminas del Rancho
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(index = 0)
                            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                            .padding(bottom = 6.dp)
                            .background(
                                if (isDarkTheme) Color(0xFF3E2D26) else MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(20.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .leatherStitchBorder(
                                color = if (isDarkTheme) LeatherStitchDefaults.GoldStitch.copy(alpha = 0.70f)
                                else LeatherStitchDefaults.DayThemeStitch.copy(alpha = 0.75f),
                                cornerRadius = 20.dp
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "BUSCAMINAS DEL RANCHO",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Versión 1.0.0 • Estilo Sinaloa mi pa 🇲🇽",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Un homenaje al clásico buscaminas con toda la ambientación, música y picardía de la sierra sinaloense.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Section Header: Equipo y Colaboradores
                item {
                    Text(
                        text = "EQUIPO Y AGRADECIMIENTOS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp,
                        modifier = Modifier
                            .staggeredEntrance(index = 1)
                            .padding(top = 8.dp, start = 4.dp)
                    )
                }

                // Contributor Cards
                itemsIndexed(contributors) { idx, contributor ->
                    ContributorCard(
                        contributor = contributor,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.staggeredEntrance(index = 2 + idx),
                        onOpenUrl = { url ->
                            try {
                                uriHandler.openUri(url)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                // Footer Card: Agradecimientos Especiales
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .staggeredEntrance(index = 2 + contributors.size)
                            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .padding(bottom = 5.dp)
                            .background(
                                if (isDarkTheme) Color(0xFF2C221E) else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                if (isDarkTheme) Color(0xFF5D4037).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                RoundedCornerShape(16.dp)
                            )
                            .leatherStitchBorder(
                                color = if (isDarkTheme) LeatherStitchDefaults.DarkThemeStitch.copy(alpha = 0.55f)
                                else LeatherStitchDefaults.DayThemePanelStitch,
                                cornerRadius = 16.dp
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¡Gracias por jugar, pariente! 🤠🌵",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "nov0caina Studio • Hecho en Sinaloa\n100% Offline • Cero Anuncios",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ContributorCard(
    contributor: Contributor,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onOpenUrl: (String) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(bottom = 5.dp)
            .background(
                if (isDarkTheme) Color(0xFF382924) else MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = if (isDarkTheme) Color(0xFF5D4037).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .leatherStitchBorder(
                color = if (isDarkTheme) LeatherStitchDefaults.DarkThemeStitch.copy(alpha = 0.55f)
                else LeatherStitchDefaults.DayThemeStitch.copy(alpha = 0.70f),
                cornerRadius = 16.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Member Header: Badge + Name + Role
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 3D Avatar/Emoji Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color.Black.copy(alpha = 0.25f), CircleShape)
                        .padding(bottom = 3.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    contributor.roleColor.copy(alpha = 0.85f),
                                    contributor.roleColor.copy(alpha = 0.45f)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contributor.badgeEmoji,
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contributor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = contributor.role,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = contributor.roleColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            Text(
                text = contributor.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            // Optional Social / Profile Link Button
            if (contributor.linkUrl != null && contributor.linkType != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .bounceClick(onClick = { onOpenUrl(contributor.linkUrl) })
                        .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(bottom = 4.dp)
                        .background(
                            if (isDarkTheme) Color(0xFF261D19) else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            contributor.roleColor.copy(alpha = 0.6f),
                            RoundedCornerShape(12.dp)
                        )
                        .leatherStitchBorder(
                            color = contributor.roleColor.copy(alpha = 0.45f),
                            cornerRadius = 12.dp,
                            inset = 2.5.dp,
                            dashLength = 3.5.dp,
                            gapLength = 2.5.dp
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = contributor.linkType.emoji,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = contributor.linkCustomLabel ?: contributor.linkType.defaultLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Abrir enlace",
                            tint = contributor.roleColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
