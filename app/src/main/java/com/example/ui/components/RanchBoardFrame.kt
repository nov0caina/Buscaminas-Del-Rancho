package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BoardRivetGold
import com.example.ui.theme.BoardRivetShadow
import com.example.ui.theme.BoardWoodBorderDark
import com.example.ui.theme.BoardWoodBorderLight
import com.example.ui.theme.BoardWoodHighlightDark
import com.example.ui.theme.BoardWoodHighlightLight
import com.example.ui.theme.BoardWoodSurfaceDark
import com.example.ui.theme.BoardWoodSurfaceLight

/**
 * Marco rústico campirano para el campo minado de Buscaminas del Rancho.
 * Renderiza una moldura de madera labrada con doble bisel, remaches de latón dorado
 * en las esquinas y sombras de elevación tanto en tema claro como oscuro.
 */
@Composable
fun RanchBoardFrame(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier,
    framePadding: Dp = 10.dp,
    content: @Composable () -> Unit
) {
    val cornerRadius = 14.dp
    val shape = RoundedCornerShape(cornerRadius)

    val woodBorder = if (isDarkTheme) BoardWoodBorderDark else BoardWoodBorderLight
    val woodSurface = if (isDarkTheme) BoardWoodSurfaceDark else BoardWoodSurfaceLight
    val woodHighlight = if (isDarkTheme) BoardWoodHighlightDark else BoardWoodHighlightLight

    val woodGradient = Brush.verticalGradient(
        colors = listOf(
            woodHighlight,
            woodSurface,
            woodBorder
        )
    )

    Box(
        modifier = modifier
            .shadow(elevation = 10.dp, shape = shape, spotColor = Color.Black.copy(alpha = 0.5f))
            .border(width = 2.5.dp, color = woodBorder, shape = shape)
            .background(brush = woodGradient, shape = shape)
            .clip(shape)
    ) {
        // Lienzo decorativo para esquineros remachados de latón y bisel de profundidad
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val rivetRadius = 3.5.dp.toPx()
            val rivetOffset = 7.dp.toPx()

            // Bisel interior de profundidad
            val innerTopLeft = Offset(framePadding.toPx() - 2.dp.toPx(), framePadding.toPx() - 2.dp.toPx())
            val innerBottomRight = Offset(w - framePadding.toPx() + 2.dp.toPx(), h - framePadding.toPx() + 2.dp.toPx())

            // Sombra interior en cuadrícula
            drawLine(
                color = Color.Black.copy(alpha = 0.45f),
                start = Offset(innerTopLeft.x, innerTopLeft.y),
                end = Offset(innerBottomRight.x, innerTopLeft.y),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color.Black.copy(alpha = 0.45f),
                start = Offset(innerTopLeft.x, innerTopLeft.y),
                end = Offset(innerTopLeft.x, innerBottomRight.y),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = if (isDarkTheme) 0.12f else 0.25f),
                start = Offset(innerTopLeft.x, innerBottomRight.y),
                end = Offset(innerBottomRight.x, innerBottomRight.y),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = if (isDarkTheme) 0.12f else 0.25f),
                start = Offset(innerBottomRight.x, innerTopLeft.y),
                end = Offset(innerBottomRight.x, innerBottomRight.y),
                strokeWidth = 1.5.dp.toPx()
            )

            // Remaches de latón en las 4 esquinas de la moldura
            val rivetPositions = listOf(
                Offset(rivetOffset, rivetOffset),
                Offset(w - rivetOffset, rivetOffset),
                Offset(rivetOffset, h - rivetOffset),
                Offset(w - rivetOffset, h - rivetOffset)
            )

            for (pos in rivetPositions) {
                // Sombra del remache
                drawCircle(
                    color = BoardRivetShadow,
                    radius = rivetRadius + 1.dp.toPx(),
                    center = Offset(pos.x + 0.5f, pos.y + 0.5f)
                )
                // Cuerpo del remache dorado
                drawCircle(
                    color = BoardRivetGold,
                    radius = rivetRadius,
                    center = pos
                )
                // Brillo superior del remache
                drawCircle(
                    color = Color.White.copy(alpha = 0.65f),
                    radius = rivetRadius * 0.4f,
                    center = Offset(pos.x - rivetRadius * 0.3f, pos.y - rivetRadius * 0.3f)
                )
            }
        }

        // Contenedor del contenido (Grid del tablero)
        Box(
            modifier = Modifier.padding(framePadding)
        ) {
            content()
        }
    }
}

@Preview
@Composable
fun RanchBoardFramePreview() {
    RanchBoardFrame(isDarkTheme = true) {
        Box(
            modifier = Modifier
                .padding(16.dp)
        )
    }
}

