package com.example.ui.particles

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Representa una partícula mutable reutilizable administrada por un Object Pool.
 * Almacena propiedades primitivas para garantizar CERO asignaciones de memoria (zero-allocations)
 * durante el ciclo de renderizado continuo en DrawScope.
 */
class Particle {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var startX: Float = 0f
    var startY: Float = 0f
    var targetX: Float = 0f
    var targetY: Float = 0f
    var radius: Float = 0f
    var maxRadius: Float = 0f
    var alpha: Float = 0f
    var maxAlpha: Float = 1f
    var color: Color = Color.Unspecified
    var secondaryColor: Color = Color.Unspecified
    var delay: Float = 0f
    var type: Int = TYPE_PUFF
    var customParam: Float = 0f

    fun reset() {
        active = false
        x = 0f
        y = 0f
        vx = 0f
        vy = 0f
        startX = 0f
        startY = 0f
        targetX = 0f
        targetY = 0f
        radius = 0f
        maxRadius = 0f
        alpha = 0f
        maxAlpha = 1f
        color = Color.Unspecified
        secondaryColor = Color.Unspecified
        delay = 0f
        type = TYPE_PUFF
        customParam = 0f
    }

    companion object {
        const val TYPE_HAZE = 0
        const val TYPE_PUFF = 1
        const val TYPE_SPECK = 2
        const val TYPE_FLAME = 3
        const val TYPE_SMOKE = 4
        const val TYPE_SPARK = 5
        const val TYPE_RING = 6
    }
}

/**
 * Object Pool de tamaño fijo pre-asignado para partículas.
 * Reutiliza instancias para eliminar la recolección de basura (GC pressure) y caídas de frames.
 */
class ParticlePool(val capacity: Int = 96) {
    private val pool: Array<Particle> = Array(capacity) { Particle() }
    private var freeCount: Int = capacity
    val activeList: ArrayList<Particle> = ArrayList(capacity)

    /**
     * Obtiene una partícula del pool reutilizable sin instanciar nuevo espacio en Heap.
     */
    fun acquire(): Particle? {
        if (freeCount > 0) {
            freeCount--
            val particle = pool[freeCount]
            particle.reset()
            particle.active = true
            activeList.add(particle)
            return particle
        }
        return null
    }

    /**
     * Libera todas las partículas activas y reinicia el pool para el siguiente evento.
     */
    fun releaseAll() {
        for (i in activeList.indices) {
            activeList[i].reset()
        }
        activeList.clear()
        freeCount = capacity
    }

    fun count(): Int = activeList.size
}

/**
 * Sistema de Partículas de Polvo del Desierto optimizado con Object Pool.
 * Administra la neblina difuminada, la bocanada en el origen, las nubes con retardo de onda
 * y las briznas de arena dispersadas por el viento.
 */
class DustParticleSystem(capacity: Int = 48) {
    val pool = ParticlePool(capacity)
    private var initializedClusterId: Long = -1L

    /**
     * Inicializa o reconfigura las partículas reutilizables según los límites del área descubierta.
     */
    fun setupCluster(
        clusterId: Long,
        cellCount: Int,
        originX: Float,
        originY: Float,
        centerX: Float,
        centerY: Float,
        spanW: Float,
        spanH: Float,
        maxSpan: Float,
        cellDpPx: Float
    ) {
        if (initializedClusterId == clusterId && pool.count() > 0) return
        initializedClusterId = clusterId
        pool.releaseAll()

        val isLargeArea = cellCount > 2

        // 1. Neblina global de polvo
        pool.acquire()?.apply {
            type = Particle.TYPE_HAZE
            startX = centerX
            startY = centerY
            maxRadius = if (isLargeArea) maxSpan * 0.6f else cellDpPx * 0.85f
            maxAlpha = 0.38f
            color = Color(0xFFE6CCB2)
            secondaryColor = Color(0xFFD4A373)
        }

        // 2. Bocanada focal en el origen del toque
        pool.acquire()?.apply {
            type = Particle.TYPE_PUFF
            startX = originX
            startY = originY
            maxRadius = if (isLargeArea) maxSpan * 0.38f else cellDpPx * 0.65f
            maxAlpha = 0.75f
            color = Color(0xFFD4A373)
            secondaryColor = Color(0xFFE9C46A)
            delay = 0f
        }

        // 3. Bocanadas perimetrales en onda expansiva
        val cloudCount = if (isLargeArea) (6 + cellCount.coerceAtMost(6)).coerceIn(6, 10) else 5
        val halfW = spanW * 0.45f
        val halfH = spanH * 0.45f

        for (i in 0 until cloudCount) {
            val angleDeg = (i * (360.0 / cloudCount))
            val rad = Math.toRadians(angleDeg)
            val cosR = kotlin.math.cos(rad).toFloat()
            val sinR = kotlin.math.sin(rad).toFloat()

            val radDistX = if (isLargeArea) halfW * cosR else cellDpPx * 0.55f * cosR
            val radDistY = if (isLargeArea) halfH * sinR else cellDpPx * 0.55f * sinR

            val targetPosX = centerX + radDistX
            val targetPosY = centerY + radDistY

            val dx = targetPosX - originX
            val dy = targetPosY - originY
            val distFromOrigin = kotlin.math.sqrt(dx * dx + dy * dy)
            val normDist = (distFromOrigin / maxSpan.coerceAtLeast(1f)).coerceIn(0f, 1f)

            pool.acquire()?.apply {
                type = Particle.TYPE_PUFF
                startX = centerX
                startY = centerY
                targetX = radDistX
                targetY = radDistY
                maxRadius = if (isLargeArea) cellDpPx * 1.0f else cellDpPx * 0.48f
                maxAlpha = 0.78f
                delay = normDist * 0.20f
                color = when (i % 4) {
                    0 -> Color(0xFFD4A373)
                    1 -> Color(0xFFE9C46A)
                    2 -> Color(0xFFB08968)
                    else -> Color(0xFFCCD5AE)
                }
                customParam = i.toFloat()
            }
        }

        // 4. Granos y briznas de arena fina
        val speckCount = if (isLargeArea) 18 else 8
        for (j in 0 until speckCount) {
            val sAngle = Math.toRadians((j * (360.0 / speckCount)) + (j * 17.0))
            val cosA = kotlin.math.cos(sAngle).toFloat()
            val sinA = kotlin.math.sin(sAngle).toFloat()

            val sDist = if (isLargeArea) maxSpan * 0.7f else cellDpPx * 1.3f

            pool.acquire()?.apply {
                type = Particle.TYPE_SPECK
                startX = centerX
                startY = centerY
                targetX = (sDist * cosA) + (j % 4 * 6f)
                targetY = (sDist * sinA) + (j % 3 * 3f)
                maxRadius = 2.8f
                maxAlpha = 0.85f
                color = if (j % 2 == 0) Color(0xFFB08968) else Color(0xFFE9C46A)
            }
        }
    }

    /**
     * Dibuja y actualiza en lote todas las partículas activas sin asignar memoria por frame.
     */
    fun render(drawScope: DrawScope, globalProgress: Float) {
        val particles = pool.activeList
        val pCount = particles.size
        for (i in 0 until pCount) {
            val p = particles[i]
            if (!p.active) continue

            when (p.type) {
                Particle.TYPE_HAZE -> {
                    val hazeAlpha = if (globalProgress < 0.2f) {
                        (globalProgress / 0.2f) * p.maxAlpha
                    } else {
                        ((1f - globalProgress) / 0.8f).coerceIn(0f, 1f) * p.maxAlpha
                    }
                    if (hazeAlpha > 0.01f) {
                        val currentRadius = p.maxRadius * (0.6f + 0.5f * globalProgress)
                        val cy = p.startY - globalProgress * 16f
                        val centerOffset = Offset(p.startX, cy)

                        drawScope.drawCircle(
                            color = p.color.copy(alpha = hazeAlpha * 0.45f),
                            radius = currentRadius,
                            center = centerOffset
                        )
                        drawScope.drawCircle(
                            color = p.secondaryColor.copy(alpha = hazeAlpha * 0.7f),
                            radius = currentRadius * 0.65f,
                            center = centerOffset
                        )
                    }
                }
                Particle.TYPE_PUFF -> {
                    val localProgress = if (p.delay > 0f) {
                        ((globalProgress - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
                    } else {
                        globalProgress
                    }

                    val puffAlpha = if (localProgress < 0.2f) {
                        (localProgress / 0.2f) * p.maxAlpha
                    } else {
                        ((1f - localProgress) / 0.8f).coerceIn(0f, 1f) * p.maxAlpha
                    }

                    if (puffAlpha > 0.01f) {
                        val currentRadius = p.maxRadius * (1f + 0.35f * (1f - localProgress))
                        val cx = p.startX + (p.targetX * (0.32f + 0.72f * globalProgress))
                        val cy = p.startY + (p.targetY * (0.32f + 0.72f * globalProgress)) - (globalProgress * 12f)
                        val puffCenter = Offset(cx, cy)

                        drawScope.drawCircle(
                            color = p.color.copy(alpha = puffAlpha * 0.35f),
                            radius = currentRadius,
                            center = puffCenter
                        )
                        drawScope.drawCircle(
                            color = p.color.copy(alpha = puffAlpha * 0.85f),
                            radius = currentRadius * 0.6f,
                            center = puffCenter
                        )
                    }
                }
                Particle.TYPE_SPECK -> {
                    val sAlpha = (1f - globalProgress).coerceIn(0f, 1f) * p.maxAlpha
                    if (sAlpha > 0.02f) {
                        val cx = p.startX + (p.targetX * globalProgress * 1.3f)
                        val cy = p.startY + (p.targetY * globalProgress * 1.3f) - (globalProgress * 18f)
                        val sRadius = (p.maxRadius * (1f - globalProgress)).coerceAtLeast(0.8f)

                        drawScope.drawCircle(
                            color = p.color.copy(alpha = sAlpha),
                            radius = sRadius,
                            center = Offset(cx, cy)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sistema de Partículas de Explosión de Pantalla optimizado con Object Pool.
 * Administra el flash inicial, bolas de fuego concéntricas, columnas de humo turbulento
 * y chispas con gravedad.
 */
class ExplosionParticleSystem(capacity: Int = 64) {
    val pool = ParticlePool(capacity)
    private var isConfigured: Boolean = false

    /**
     * Pre-configura todas las partículas reutilizables para la explosión.
     */
    fun setupExplosion(centerX: Float, centerY: Float, maxScreenRadius: Float) {
        if (isConfigured && pool.count() > 0) return
        isConfigured = true
        pool.releaseAll()

        // 1. Capas concéntricas de fuego (Flames)
        // Núcleo incandescente
        pool.acquire()?.apply {
            type = Particle.TYPE_FLAME
            startX = centerX
            startY = centerY
            maxRadius = maxScreenRadius * 0.22f
            maxAlpha = 1.0f
            color = Color(0xFFFFFBEB)
            customParam = 0.22f
        }
        // Llama interna dorada
        pool.acquire()?.apply {
            type = Particle.TYPE_FLAME
            startX = centerX
            startY = centerY
            maxRadius = maxScreenRadius * 0.44f
            maxAlpha = 0.96f
            color = Color(0xFFFFD166)
            customParam = 0.44f
        }
        // Llama intermedia naranja
        pool.acquire()?.apply {
            type = Particle.TYPE_FLAME
            startX = centerX
            startY = centerY
            maxRadius = maxScreenRadius * 0.68f
            maxAlpha = 0.92f
            color = Color(0xFFFF9F1C)
            customParam = 0.68f
        }
        // Llama exterior carmesí
        pool.acquire()?.apply {
            type = Particle.TYPE_FLAME
            startX = centerX
            startY = centerY
            maxRadius = maxScreenRadius * 0.92f
            maxAlpha = 0.85f
            color = Color(0xFFD62828)
            customParam = 0.92f
        }

        // 2. Anillo de onda expansiva
        pool.acquire()?.apply {
            type = Particle.TYPE_RING
            startX = centerX
            startY = centerY
            maxRadius = maxScreenRadius * 1.1f
            maxAlpha = 0.80f
            color = Color(0xFFFFD166)
        }

        // 3. Columnas volumétricas de humo
        val smokeDark = Color(0xFF15181E)
        val smokeCharcoal = Color(0xFF282D36)
        val smokeAsh = Color(0xFF404754)
        val smokePlume = Color(0xFF5E6778)

        val smokeAngles = floatArrayOf(0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f)
        val smokeFactors = floatArrayOf(0.55f, 0.72f, 0.48f, 0.68f, 0.85f, 0.70f, 0.50f, 0.75f)
        val smokeColors = arrayOf(smokeDark, smokeCharcoal, smokePlume, smokeAsh, smokeDark, smokeCharcoal, smokePlume, smokeAsh)

        for (i in smokeAngles.indices) {
            pool.acquire()?.apply {
                type = Particle.TYPE_SMOKE
                startX = centerX
                startY = centerY
                customParam = smokeAngles[i]
                targetX = smokeFactors[i] // Factor de distancia
                maxRadius = maxScreenRadius * (0.30f + (i % 3) * 0.05f)
                maxAlpha = 0.90f
                color = smokeColors[i]
            }
        }

        // 4. Chispas y brasas incandescentes con física
        val sparkAngles = floatArrayOf(15f, 45f, 75f, 105f, 135f, 165f, 195f, 225f, 255f, 285f, 315f, 345f)
        for (j in sparkAngles.indices) {
            pool.acquire()?.apply {
                type = Particle.TYPE_SPARK
                startX = centerX
                startY = centerY
                customParam = sparkAngles[j]
                targetX = 0.65f + (j % 4) * 0.18f // Factor de velocidad
                maxRadius = 4.2f
                maxAlpha = 0.95f
                color = when (j % 3) {
                    0 -> Color(0xFFFF9F1C)
                    1 -> Color(0xFFFFD166)
                    else -> Color(0xFFFF5722)
                }
            }
        }
    }

    /**
     * Dibuja y actualiza la explosión completa a través del pool con cero asignaciones en DrawScope.
     */
    fun render(drawScope: DrawScope, progress: Float, width: Float, height: Float) {
        val center = Offset(width / 2f, height / 2f)
        val maxScreenRadius = kotlin.math.max(width, height) * 0.8f

        // 0. Micro-flash blanco inicial
        if (progress < 0.08f) {
            val whiteFlashAlpha = ((1f - (progress / 0.08f)) * 0.85f).coerceIn(0f, 0.85f)
            drawScope.drawRect(
                color = Color.White.copy(alpha = whiteFlashAlpha),
                size = drawScope.size
            )
        }

        // 1. Resplandor ambiental de ignición
        if (progress < 0.40f) {
            val blastProgress = progress / 0.40f
            val blastAlpha = (1f - blastProgress) * 0.95f
            drawScope.drawRect(
                color = Color(0xFFFF9F1C).copy(alpha = blastAlpha * 0.38f),
                size = drawScope.size
            )
        }

        val particles = pool.activeList
        val pCount = particles.size
        for (i in 0 until pCount) {
            val p = particles[i]
            if (!p.active) continue

            when (p.type) {
                Particle.TYPE_FLAME -> {
                    if (progress < 0.40f) {
                        val blastProgress = progress / 0.40f
                        val blastAlpha = (1f - blastProgress) * p.maxAlpha
                        val currentRadius = maxScreenRadius * (0.28f + blastProgress * 0.92f) * (p.customParam / 0.92f)
                        drawScope.drawCircle(
                            color = p.color.copy(alpha = blastAlpha),
                            radius = currentRadius,
                            center = center
                        )
                    }
                }
                Particle.TYPE_RING -> {
                    if (progress < 0.40f) {
                        val blastProgress = progress / 0.40f
                        val blastAlpha = (1f - blastProgress) * p.maxAlpha
                        val ringRadius = maxScreenRadius * (0.35f + blastProgress * 1.1f)
                        drawScope.drawCircle(
                            color = p.color.copy(alpha = blastAlpha),
                            radius = ringRadius,
                            center = center,
                            style = Stroke(width = 12f * (1f - blastProgress))
                        )
                    }
                }
                Particle.TYPE_SMOKE -> {
                    val smokeAlpha = if (progress < 0.20f) {
                        (progress / 0.20f) * p.maxAlpha
                    } else {
                        ((1f - progress) / 0.80f).coerceIn(0f, 1f) * p.maxAlpha
                    }

                    if (smokeAlpha > 0.01f) {
                        val deg = p.customParam
                        val distFactor = p.targetX
                        val angleOffset = if (i % 2 == 0) progress * 24.0 else -progress * 20.0
                        val rad = Math.toRadians(deg.toDouble() + angleOffset)
                        val drift = maxScreenRadius * (0.20f + progress * 0.82f) * distFactor
                        val plumeX = center.x + (drift * kotlin.math.cos(rad)).toFloat()
                        val plumeY = center.y + (drift * kotlin.math.sin(rad)).toFloat() - (progress * 90f * distFactor)
                        val plumeOffset = Offset(plumeX, plumeY)
                        val radius = p.maxRadius * (1f + progress * 1.4f)

                        drawScope.drawCircle(
                            color = p.color.copy(alpha = smokeAlpha * 0.45f),
                            radius = radius,
                            center = plumeOffset
                        )
                        drawScope.drawCircle(
                            color = p.color.copy(alpha = smokeAlpha * 0.88f),
                            radius = radius * 0.62f,
                            center = plumeOffset
                        )
                    }
                }
                Particle.TYPE_SPARK -> {
                    val emberAlpha = (1f - progress).coerceIn(0f, 1f) * p.maxAlpha
                    if (emberAlpha > 0.05f) {
                        val deg = p.customParam
                        val speed = p.targetX
                        val rad = Math.toRadians(deg.toDouble())
                        val dist = maxScreenRadius * (progress * 1.4f * speed)
                        val sparkX = center.x + (dist * kotlin.math.cos(rad)).toFloat()
                        val sparkY = center.y + (dist * kotlin.math.sin(rad)).toFloat() - (progress * 55f) + (progress * progress * 95f)
                        val sparkOffset = Offset(sparkX, sparkY)
                        val sparkRadius = (p.maxRadius * (1f - progress * 0.7f)).coerceAtLeast(1.0f)

                        drawScope.drawCircle(
                            color = p.color.copy(alpha = emberAlpha),
                            radius = sparkRadius,
                            center = sparkOffset
                        )
                    }
                }
            }
        }
    }
}
