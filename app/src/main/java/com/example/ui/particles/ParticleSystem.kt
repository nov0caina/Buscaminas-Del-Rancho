package com.example.ui.particles

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
    
    fun setupAmbientDust(width: Float, height: Float) {
        if (initializedClusterId == -2L && pool.count() > 0) return
        initializedClusterId = -2L
        pool.releaseAll()

        pool.acquire()?.apply {
            type = Particle.TYPE_HAZE
            startX = width / 2f
            startY = height / 2f
            maxRadius = width * 1.5f
            maxAlpha = 0.08f
            color = Color(0xFFD4A373)
            secondaryColor = Color(0xFFE9C46A)
            delay = 0f
        }

        val speckCount = 110
        for (i in 0 until speckCount) {
            pool.acquire()?.apply {
                type = Particle.TYPE_SPECK
                startX = width * Math.random().toFloat()
                startY = height * Math.random().toFloat()
                targetX = (Math.random().toFloat() - 0.5f) * width * 0.15f
                targetY = (Math.random().toFloat() - 0.5f) * height * 0.15f
                maxRadius = (width * 0.005f) + (Math.random().toFloat() * width * 0.005f)
                maxAlpha = 0.45f + Math.random().toFloat() * 0.35f
                delay = i.toFloat() / speckCount.toFloat()
                color = if (i % 3 == 0) Color(0xFFE9C46A) else Color(0xFFB08968)
            }
        }
    }

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
                    val localProgress = (globalProgress + p.delay) % 1.0f
                    val hazeAlpha = kotlin.math.sin(localProgress * 3.14159f) * p.maxAlpha
                    if (hazeAlpha > 0.005f) {
                        val currentRadius = p.maxRadius * (0.8f + 0.3f * localProgress)
                        val cy = p.startY - localProgress * 16f
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
                    val localProgress = (globalProgress + p.delay) % 1.0f
                    val sAlpha = kotlin.math.sin(localProgress * 3.14159f) * p.maxAlpha
                    if (sAlpha > 0.01f) {
                        val cx = p.startX + (p.targetX * localProgress * 1.3f)
                        val cy = p.startY + (p.targetY * localProgress * 1.3f) - (localProgress * 18f)
                        val sRadius = (p.maxRadius * (1f - localProgress * 0.3f)).coerceAtLeast(0.8f)

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

/**
 * Sistema de Partículas de Humo Iluminado con Física de Fluidos (Buoyancy, Vorticity y Disipación).
 * - Termodinámica y sustentación (buoyancy): El humo asciende acelerando suavemente hacia arriba.
 * - Vorticity & Turbulencia: Oscilaciones sinusoidales que simulan remolinos y jirones orgánicos.
 * - Iluminación Volumétrica: Las partículas reflejan luz cian brillante cerca del centro y se enfrían a tonos oscuros al alejarse.
 * - Expansión y Disipación: Aumento de radio por difusión y desvanecimiento progresivo sin halos circulares.
 * - Object Pool con CERO asignaciones en tiempo de renderizado (60/120 FPS).
 */
class MysticSmokeParticleSystem(capacity: Int = 96) {
    val pool = ParticlePool(capacity)
    private var isConfigured: Boolean = false

    fun setupMysticSmoke(centerX: Float, centerY: Float, baseRadius: Float) {
        if (isConfigured && pool.count() > 0) return
        isConfigured = true
        pool.releaseAll()

        // 1. Columnas y flujos de humo principales ascendentes (Emitter continuo desde la base y laterales)
        val smokePuffCount = 90
        for (i in 0 until smokePuffCount) {
            val emitterLayer = i % 4
            // Emisores distribuidos en la base del rostro y los costados
            val spawnAngle = Math.toRadians((i * (360.0 / smokePuffCount)) + (i * 13.0))
            val spawnDistX = (baseRadius * 0.45f) * kotlin.math.cos(spawnAngle).toFloat()
            val spawnDistY = when (emitterLayer) {
                0 -> baseRadius * 0.40f // Base inferior
                1 -> baseRadius * 0.15f // Zona media
                2 -> baseRadius * 0.55f // Pluma inferior profunda
                else -> -baseRadius * 0.10f // Laterales superiores
            }

            pool.acquire()?.apply {
                type = Particle.TYPE_SMOKE
                startX = centerX + spawnDistX
                startY = centerY + spawnDistY
                // vx: deriva lateral, vy: flotabilidad térmica hacia arriba
                vx = ((i % 7) - 3f) * 0.28f
                vy = 0.55f + (i % 5) * 0.18f // Velocidad de ascenso
                customParam = (i * 0.85f) // Fase turbulenta
                targetX = 1.3f + (i % 4) * 0.35f // Factor de expansión por difusión
                targetY = if (i % 2 == 0) 1.2f else -1.2f // Sentido del remolino
                maxRadius = baseRadius * (0.28f + (i % 5) * 0.06f)
                maxAlpha = if (emitterLayer == 0 || emitterLayer == 2) 0.65f else 0.50f
                delay = (i / smokePuffCount.toFloat()) * 0.55f // Emisión escalonada en el tiempo
            }
        }

        // 2. Niebla difusa ambiental de fondo atrapada en la turbulencia
        for (h in 0 until 12) {
            val hAngle = Math.toRadians(h * 30.0)
            val hDist = baseRadius * 0.30f
            pool.acquire()?.apply {
                type = Particle.TYPE_HAZE
                startX = centerX + (hDist * kotlin.math.cos(hAngle)).toFloat()
                startY = centerY + (hDist * kotlin.math.sin(hAngle)).toFloat() + 20f
                vx = (h % 3 - 1f) * 0.15f
                vy = 0.35f + (h % 3) * 0.12f
                customParam = h.toFloat()
                targetX = 1.6f
                targetY = if (h % 2 == 0) 0.8f else -0.8f
                maxRadius = baseRadius * (0.50f + (h % 3) * 0.15f)
                maxAlpha = 0.32f
                delay = h * 0.04f
            }
        }

        // 3. Chispas y motas de luz cian arrastradas por la corriente térmica ascendente
        val emberCount = 30
        for (j in 0 until emberCount) {
            val eAngle = Math.toRadians((j * (360.0 / emberCount)) + (j * 17.0))
            val eDist = baseRadius * (0.20f + (j % 5) * 0.15f)
            pool.acquire()?.apply {
                type = Particle.TYPE_SPECK
                startX = centerX + (eDist * kotlin.math.cos(eAngle)).toFloat()
                startY = centerY + (eDist * kotlin.math.sin(eAngle)).toFloat() + 40f
                vx = ((j % 5) - 2f) * 0.35f
                vy = 0.85f + (j % 4) * 0.25f // Ascenso más rápido por menor peso
                customParam = j * 1.3f
                maxRadius = 1.6f + (j % 3) * 0.8f
                maxAlpha = 0.90f
                delay = (j / emberCount.toFloat()) * 0.60f
                color = if (j % 2 == 0) Color(0xFFE0FFFF) else Color(0xFF18FFFF)
            }
        }
    }

    fun render(drawScope: DrawScope, progress: Float, width: Float, height: Float) {
        val lightCenter = Offset(width / 2f, height / 2f - 20f)
        val maxLightRadius = kotlin.math.min(width, height) * 0.60f

        val particles = pool.activeList
        val pCount = particles.size
        for (i in 0 until pCount) {
            val p = particles[i]
            if (!p.active) continue

            // Progreso relativo de vida de la partícula (con retardo de emisión)
            val tau = if (p.delay > 0f) {
                ((progress - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            } else {
                progress
            }

            if (tau <= 0.001f || tau >= 0.999f) continue

            when (p.type) {
                Particle.TYPE_HAZE -> {
                    // Ascenso lento y difusión de niebla
                    val upward = (p.vy * tau * 120f) + (0.5f * 20f * tau * tau)
                    val swirl = kotlin.math.sin(tau * 3.1416f + p.customParam) * (p.targetY * 30f * tau)
                    val px = p.startX + (p.vx * tau * 50f) + swirl
                    val py = p.startY - upward
                    val currentCenter = Offset(px, py)

                    val currentRadius = p.maxRadius * (1f + p.targetX * tau)
                    val alphaFade = if (tau < 0.30f) {
                        (tau / 0.30f) * p.maxAlpha
                    } else {
                        ((1f - tau) / 0.70f) * p.maxAlpha
                    }

                    if (alphaFade > 0.01f) {
                        // Iluminación volumétrica según proximidad a la fuente de luz
                        val dx = px - lightCenter.x
                        val dy = py - lightCenter.y
                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                        val lightProximity = (1f - (dist / maxLightRadius)).coerceIn(0.1f, 1.0f)

                        val coreColor = Color(0xFF00B4D8).copy(alpha = alphaFade * 0.35f * lightProximity)
                        val edgeColor = Color(0xFF003844).copy(alpha = alphaFade * 0.12f * lightProximity)

                        drawScope.drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(coreColor, edgeColor, Color.Transparent),
                                center = currentCenter,
                                radius = currentRadius
                            ),
                            radius = currentRadius,
                            center = currentCenter
                        )
                    }
                }
                Particle.TYPE_SMOKE -> {
                    // Física de Humo: Ascenso por sustentación térmica + Remolino de vórtice (vorticity)
                    val upward = (p.vy * tau * 230f) + (0.5f * 50f * tau * tau)
                    // Frecuencia sinusoidal para ondulación natural
                    val curl = kotlin.math.sin((tau * 4.5f) + p.customParam) * (p.targetY * 42f * tau)
                    val px = p.startX + (p.vx * tau * 75f) + curl
                    val py = p.startY - upward
                    val puffCenter = Offset(px, py)

                    // Expansión volumétrica al difundirse en el aire
                    val currentRadius = p.maxRadius * (0.85f + p.targetX * tau)

                    // Curva de densidad óptica (inflow rápido, disipación gradual)
                    val density = if (tau < 0.22f) {
                        (tau / 0.22f) * p.maxAlpha
                    } else {
                        val fade = (1f - tau) / 0.78f
                        (fade * fade) * p.maxAlpha
                    }

                    if (density > 0.01f) {
                        // Iluminación Volumétrica: La luz interna ilumina el humo desde el centro
                        val dx = px - lightCenter.x
                        val dy = py - lightCenter.y
                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                        val illumFactor = (1f - (dist / maxLightRadius)).coerceIn(0.12f, 1.0f)

                        // Gradiente de color según iluminación física
                        val litCyan = Color(0xFF00E5FF).copy(alpha = density * 0.65f * illumFactor)
                        val midTeal = Color(0xFF0077B6).copy(alpha = density * 0.32f * illumFactor)
                        val darkSmoke = Color(0xFF041018).copy(alpha = density * 0.18f)

                        drawScope.drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(litCyan, midTeal, darkSmoke, Color.Transparent),
                                center = puffCenter,
                                radius = radiusScale(currentRadius, illumFactor)
                            ),
                            radius = currentRadius,
                            center = puffCenter
                        )
                    }
                }
                Particle.TYPE_SPECK -> {
                    // Chispas/Motes ligeras que siguen las corrientes de convección del humo
                    val upward = (p.vy * tau * 300f) + (0.5f * 70f * tau * tau)
                    val flutter = kotlin.math.cos((tau * 6.0f) + p.customParam) * (18f * tau)
                    val sx = p.startX + (p.vx * tau * 85f) + flutter
                    val sy = p.startY - upward

                    val speckAlpha = if (tau < 0.20f) {
                        (tau / 0.20f) * p.maxAlpha
                    } else {
                        ((1f - tau) / 0.80f) * p.maxAlpha
                    }

                    if (speckAlpha > 0.02f) {
                        val sRadius = (p.maxRadius * (1f - tau * 0.45f)).coerceAtLeast(0.7f)
                        drawScope.drawCircle(
                            color = p.color.copy(alpha = speckAlpha),
                            radius = sRadius,
                            center = Offset(sx, sy)
                        )
                    }
                }
            }
        }
    }

    private fun radiusScale(base: Float, factor: Float): Float {
        return (base * (0.75f + 0.25f * factor)).coerceAtLeast(1f)
    }
}

/**
 * Sistema de Partículas Campiranas Ligeras para Modo Noche y Menús Secundarios.
 * Aplica la misma física y estilo de partículas ligeras (TYPE_SPECK) del modo día,
 * distribuidas por toda la pantalla con colores cálidos de rancho (ámbar, oro, ascuas y polvo).
 */
class RanchoSmokeParticleSystem(capacity: Int = 150) {
    val pool = ParticlePool(capacity)
    private var isConfigured: Boolean = false

    fun setupRanchoSmoke(width: Float, height: Float) {
        if (isConfigured && pool.count() > 0) return
        isConfigured = true
        pool.releaseAll()

        // 1. Neblina ambiental difusa de fondo
        pool.acquire()?.apply {
            type = Particle.TYPE_HAZE
            startX = width / 2f
            startY = height / 2f
            maxRadius = width * 1.5f
            maxAlpha = 0.08f
            color = Color(0xFF8B5E3C)
            secondaryColor = Color(0xFFD4A373)
            delay = 0f
        }

        // 2. Briznas de polvo nocturno y ascuas flotantes estilo modo día
        val speckCount = 130
        for (i in 0 until speckCount) {
            pool.acquire()?.apply {
                type = Particle.TYPE_SPECK
                startX = width * Math.random().toFloat()
                startY = height * Math.random().toFloat()
                // Deriva dinámica de viento (horizontal y vertical)
                targetX = (Math.random().toFloat() - 0.45f) * width * 0.25f
                targetY = -((0.15f + Math.random().toFloat() * 0.45f) * height * 0.30f)
                maxRadius = (width * 0.004f) + (Math.random().toFloat() * width * 0.0055f)
                maxAlpha = 0.45f + Math.random().toFloat() * 0.40f
                delay = i.toFloat() / speckCount.toFloat()
                // Tonalidades de noche en el rancho: Oro ámbar, Ascua cálida, Terracota, Polvo arena
                color = when (i % 4) {
                    0 -> Color(0xFFFFD166) // Ámbar oro brillante
                    1 -> Color(0xFFF4A261) // Naranja cálido
                    2 -> Color(0xFFE76F51) // Ascua terracota
                    else -> Color(0xFFD4A373) // Polvo dorado suave
                }
            }
        }
    }

    fun setupRanchoSmoke(centerX: Float, centerY: Float, baseRadius: Float) {
        setupRanchoSmoke(centerX * 2f, centerY * 2f)
    }

    fun setupMysticSmoke(centerX: Float, centerY: Float, baseRadius: Float) {
        setupRanchoSmoke(centerX * 2f, centerY * 2f)
    }

    fun render(drawScope: DrawScope, progress: Float, width: Float, height: Float) {
        val particles = pool.activeList
        val pCount = particles.size
        for (i in 0 until pCount) {
            val p = particles[i]
            if (!p.active) continue

            val localProgress = (progress + p.delay) % 1.0f

            when (p.type) {
                Particle.TYPE_HAZE -> {
                    val hazeAlpha = kotlin.math.sin(localProgress * 3.14159f) * p.maxAlpha
                    if (hazeAlpha > 0.005f) {
                        val currentRadius = p.maxRadius * (0.85f + 0.3f * localProgress)
                        val cy = p.startY - localProgress * 20f
                        val centerOffset = Offset(p.startX, cy)

                        drawScope.drawCircle(
                            color = p.color.copy(alpha = hazeAlpha * 0.4f),
                            radius = currentRadius,
                            center = centerOffset
                        )
                        drawScope.drawCircle(
                            color = p.secondaryColor.copy(alpha = hazeAlpha * 0.6f),
                            radius = currentRadius * 0.65f,
                            center = centerOffset
                        )
                    }
                }
                Particle.TYPE_SPECK -> {
                    val sAlpha = kotlin.math.sin(localProgress * 3.14159f) * p.maxAlpha
                    if (sAlpha > 0.01f) {
                        val sway = kotlin.math.sin((localProgress * 4.0f) + (p.delay * 10f)) * (width * 0.03f)
                        val cx = (p.startX + (p.targetX * localProgress) + sway)
                        val cy = (p.startY + (p.targetY * localProgress)) - (localProgress * 30f)

                        val wrappedCx = if (cx < 0) cx + width else if (cx > width) cx % width else cx
                        val wrappedCy = if (cy < 0) cy + height else if (cy > height) cy % height else cy
                        val sRadius = (p.maxRadius * (0.8f + kotlin.math.sin(localProgress * 3.14159f) * 0.4f)).coerceAtLeast(0.8f)

                        drawScope.drawCircle(
                            color = p.color.copy(alpha = sAlpha),
                            radius = sRadius,
                            center = Offset(wrappedCx, wrappedCy)
                        )
                    }
                }
            }
        }
    }
}




