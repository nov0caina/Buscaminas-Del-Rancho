package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Rancho Día (Light Theme Palette)
val RanchoBannerTitleDay = Color(0xFFF39C12)         // Amarillo oro / Ámbar brillante (Título principal del banner en modo día)
val RanchoOchrePrimary = Color(0xFF8B5E3C)           // Café / Ocre tierra
val RanchoOchreOnPrimary = Color(0xFFFFFFFF)         // Blanco puro
val RanchoGoldContainer = Color(0xFFFFE0B2)          // Amarillo trigo / Oro claro
val RanchoGoldOnContainer = Color(0xFF4E2600)        // Café oscuro tostado

val RanchoCactusSecondary = Color(0xFF2E7D32)        // Verde cactus
val RanchoCactusContainer = Color(0xFFC8E6C9)        // Verde menta suave
val RanchoCactusOnContainer = Color(0xFF003300)      // Verde bosque profundo

val RanchoTerracottaTertiary = Color(0xFFC0392B)     // Rojo terracota / Ladrillo
val RanchoTerracottaContainer = Color(0xFFFFCDD2)    // Rosa claro / Arcilla suave

val RanchoSunBackground = Color(0xFFEDE3D4)          // Pergamino cálido / Arena desértica suave (anti-fatiga visual)
val RanchoWoodSurface = Color(0xFFE2D5C3)            // Madera suave / Arena cálida al sol
val RanchoSandSurfaceVariant = Color(0xFFD5C6B1)     // Arena tostada / Cuero suave

// Rancho Noche (Dark Theme Palette)
val RanchoBannerTitleNight = Color(0xFFF39C12)           // Amarillo oro / Ámbar brillante
val RanchoNightPrimary = Color(0xFFF39C12)           // Amarillo oro / Ámbar brillante
val RanchoNightOnPrimary = Color(0xFF211204)         // Café muy oscuro / Negro cálido
val RanchoNightPrimaryContainer = Color(0xFF5D4037)  // Café cuero / Madera nogal

val RanchoNightCactusSecondary = Color(0xFF4CAF50)   // Verde brillante / Neón campirano
val RanchoNightCactusContainer = Color(0xFF1B5E20)   // Verde pino nocturno

val RanchoNightTerracottaTertiary = Color(0xFFE74C3C)// Rojo fuego / Carmín

val RanchoNightBackground = Color(0xFF141210)        // Negro noche / Asfalto profundo
val RanchoNightSurface = Color(0xFF221E1B)           // Carbón cálido / Cuero oscuro
val RanchoNightSurfaceVariant = Color(0xFF332B25)    // Café cenizo oscuro / Madera noche

// Mine Board Number Colors (Day Mode: Classic rich contrast on light earth)
val Number1Blue = Color(0xFF0D47A1)                  // Azul mezclilla profundo (>6:1 contraste sobre tierra beige)
val Number2Green = Color(0xFF2E7D32)                 // Verde bosque
val Number3Red = Color(0xFFC62828)                   // Rojo
val Number4Purple = Color(0xFF6A1B9A)                // Morado / Púrpura
val Number5Maroon = Color(0xFF800000)                // Marrón / Granate
val Number6Teal = Color(0xFF00838F)                  // Turquesa / Verde azulado
val Number7Black = Color(0xFF212121)                 // Negro
val Number8Gray = Color(0xFF616161)                  // Gris

// Mine Board Number Colors (Night Mode: Luminous neon tones with >7:1 WCAG AAA contrast on #2E2722)
val Number1BlueNight = Color(0xFF64B5F6)             // Azul cielo luminoso
val Number2GreenNight = Color(0xFF81C784)            // Verde menta claro
val Number3RedNight = Color(0xFFE57373)              // Coral vibrante
val Number4PurpleNight = Color(0xFFBA68C8)           // Lavanda claro
val Number5AmberNight = Color(0xFFFFB74D)            // Ámbar brillante
val Number6TealNight = Color(0xFF4DD0E1)             // Turquesa claro
val Number7CreamNight = Color(0xFFFFF9C4)            // Crema suave
val Number8SilverNight = Color(0xFFECEFF1)           // Platino / Plata brillante

fun getNumberColor(number: Int, isDarkTheme: Boolean = false): Color = if (isDarkTheme) {
    when (number) {
        1 -> Number1BlueNight
        2 -> Number2GreenNight
        3 -> Number3RedNight
        4 -> Number4PurpleNight
        5 -> Number5AmberNight
        6 -> Number6TealNight
        7 -> Number7CreamNight
        8 -> Number8SilverNight
        else -> Color.White
    }
} else {
    when (number) {
        1 -> Number1Blue
        2 -> Number2Green
        3 -> Number3Red
        4 -> Number4Purple
        5 -> Number5Maroon
        6 -> Number6Teal
        7 -> Number7Black
        8 -> Number8Gray
        else -> Color.Black
    }
}

// Paleta Táctil 3D y Relieve Campirano (Board, Cells & Frame)
// Casillas no reveladas (Losa elevada con bisel)
val CellBevelHighlightDay = Color(0xFFFFF2DF)        // Reflejo de sol en bisel superior/izquierdo (Día)
val CellBevelShadowDay = Color(0xFF5D381E)           // Sombra profunda en bisel inferior/derecho (Día)
val CellBaseDay = Color(0xFF8B5E3C)                  // Ocre cuero / barro campirano (Día)

val CellBevelHighlightNight = Color(0xFFFFD180)      // Luz ámbar dorada en bisel superior/izquierdo (Noche)
val CellBevelShadowNight = Color(0xFF9E5700)         // Sombra ámbar tostada en bisel inferior/derecho (Noche)
val CellBaseNight = Color(0xFFF39C12)                // Ámbar oro clásico del rancho (Noche)

// Casillas presionadas (Press-down feedback)
val CellPressedDay = Color(0xFF724A2D)               // Tono presionado día
val CellPressedNight = Color(0xFFD68910)             // Tono presionado noche

// Terreno excavado / Casillas reveladas (Hundidas en la tierra)
val CellExcavatedDay = Color(0xFFDDD2C2)             // Tierra caliza suave excavada (Día antideslumbrante)
val CellExcavatedBorderDay = Color(0xFFC5B7A5)       // Borde interior de excavación suave (Día)
val CellExcavatedNight = Color(0xFF1E1A17)           // Tierra negra fértil del rancho (Noche)
val CellExcavatedBorderNight = Color(0xFF120F0D)     // Borde interior oscuro (Noche)

// Marco Rústico Campirano (Madera y remaches)
val BoardWoodBorderDark = Color(0xFF23170F)          // Madera roble oscuro
val BoardWoodSurfaceDark = Color(0xFF332318)         // Veta de madera oscura
val BoardWoodHighlightDark = Color(0xFF4A3425)       // Luz en la veta de madera
val BoardRivetGold = Color(0xFFE6B800)               // Remache de latón / oro viejo
val BoardRivetShadow = Color(0xFF140C07)             // Sombra del remache

val BoardWoodBorderLight = Color(0xFF5D4037)         // Madera nogal rústica
val BoardWoodSurfaceLight = Color(0xFF8D6E63)        // Madera cálida exterior
val BoardWoodHighlightLight = Color(0xFFBCAAA4)      // Reflejo en madera

