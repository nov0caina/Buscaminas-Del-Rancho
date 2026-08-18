package com.example

import com.example.ui.particles.DustParticleSystem
import com.example.ui.particles.ExplosionParticleSystem
import com.example.ui.particles.Particle
import com.example.ui.particles.ParticlePool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ParticlePoolTest {

    @Test
    fun `test ParticlePool acquire and releaseAll`() {
        val pool = ParticlePool(capacity = 10)
        assertEquals(0, pool.count())

        val p1 = pool.acquire()
        assertNotNull(p1)
        assertTrue(p1!!.active)
        assertEquals(1, pool.count())

        val p2 = pool.acquire()
        assertNotNull(p2)
        assertEquals(2, pool.count())

        pool.releaseAll()
        assertEquals(0, pool.count())
    }

    @Test
    fun `test DustParticleSystem cluster setup`() {
        val dustSystem = DustParticleSystem(capacity = 48)
        dustSystem.setupCluster(
            clusterId = 100L,
            cellCount = 8,
            originX = 50f,
            originY = 50f,
            centerX = 100f,
            centerY = 100f,
            spanW = 200f,
            spanH = 200f,
            maxSpan = 200f,
            cellDpPx = 40f
        )

        assertTrue(dustSystem.pool.count() > 0)
        assertTrue(dustSystem.pool.count() <= 48)
    }

    @Test
    fun `test ExplosionParticleSystem setup`() {
        val explosionSystem = ExplosionParticleSystem(capacity = 64)
        explosionSystem.setupExplosion(
            centerX = 200f,
            centerY = 400f,
            maxScreenRadius = 500f
        )

        assertTrue(explosionSystem.pool.count() > 0)
        assertTrue(explosionSystem.pool.count() <= 64)
    }
}
