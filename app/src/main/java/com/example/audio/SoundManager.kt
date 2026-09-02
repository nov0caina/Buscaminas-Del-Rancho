package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val PREFS_NAME = "rancho_audio_prefs"
private const val KEY_MUSIC_ENABLED = "key_music_enabled"
private const val KEY_MUSIC_VOLUME = "key_music_volume"
private const val KEY_SFX_ENABLED = "key_sfx_enabled"
private const val KEY_SFX_VOLUME = "key_sfx_volume"

class SoundManager private constructor(private val appContext: Context) {

    private val audioScope = CoroutineScope(Dispatchers.Main + Job())
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // SoundPool for low-latency SFX
    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<Int, Int>()
    private val loadedSounds = mutableSetOf<Int>()

    // Volume & Settings (Persisted)
    var isSfxEnabled: Boolean = true
        private set
    var sfxVolume: Float = 0.20f
        private set

    var isMusicEnabled: Boolean = true
        private set
    var musicVolume: Float = 0.10f
        private set

    // Soundtrack Management
    private val soundtracks = listOf(
        R.raw.soundtrack_banda_sinaloense,
        R.raw.soundtrack_corrido_tumbado
    )
    private var currentTrackIndex = 0
    private var currentPlayer: MediaPlayer? = null
    private var fadingPlayer: MediaPlayer? = null
    private var crossfadeJob: Job? = null
    private var isCrossfading: Boolean = false
    private var isAppInForeground: Boolean = true
    var hasStartedPlayback: Boolean = false
        private set

    // Sequential Non-Repeating SFX Decks
    private class SequentialAudioDeck(private val sounds: List<Int>) {
        private var currentIndex = 0
        @Synchronized
        fun next(): Int {
            if (sounds.isEmpty()) return 0
            val sound = sounds[currentIndex % sounds.size]
            currentIndex = (currentIndex + 1) % sounds.size
            return sound
        }
    }

    private val explosionDeck = SequentialAudioDeck(listOf(R.raw.explosion_01, R.raw.explosion_02, R.raw.explosion_03))
    private val defeatTrumpetDeck = SequentialAudioDeck(listOf(R.raw.lose_funny_trumpet_01, R.raw.lose_funny_trumpet_02, R.raw.lose_funny_sad_aaay))
    private val victoryCelebrationDeck = SequentialAudioDeck(listOf(R.raw.victory_celebration_01, R.raw.victory_celebration_02))
    private val waitingDeck = SequentialAudioDeck(listOf(R.raw.waiting_a_few_moments_later, R.raw.waiting_two_hours_later))

    init {
        loadAudioPreferences()
        initSoundPool()
    }

    private fun loadAudioPreferences() {
        val defaultMusicVol = 0.10f // Exact 10%
        val defaultSfxVol = 0.20f   // Exact 20%

        if (!prefs.contains(KEY_MUSIC_VOLUME)) {
            musicVolume = defaultMusicVol
            prefs.edit().putFloat(KEY_MUSIC_VOLUME, defaultMusicVol).apply()
        } else {
            musicVolume = prefs.getFloat(KEY_MUSIC_VOLUME, defaultMusicVol)
        }

        if (!prefs.contains(KEY_SFX_VOLUME)) {
            sfxVolume = defaultSfxVol
            prefs.edit().putFloat(KEY_SFX_VOLUME, defaultSfxVol).apply()
        } else {
            sfxVolume = prefs.getFloat(KEY_SFX_VOLUME, defaultSfxVol)
        }

        isMusicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true)
        isSfxEnabled = prefs.getBoolean(KEY_SFX_ENABLED, true)
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build().apply {
                setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0) {
                        loadedSounds.add(sampleId)
                    }
                }
            }

        // Preload all SFX
        val allSfx = listOf(
            R.raw.pop_01,
            R.raw.pop_double_01,
            R.raw.pop_double_02,
            R.raw.explosion_01,
            R.raw.explosion_02,
            R.raw.explosion_03,
            R.raw.lose_funny_sad_aaay,
            R.raw.lose_funny_trumpet_01,
            R.raw.lose_funny_trumpet_02,
            R.raw.victory_woooow,
            R.raw.victory_celebration_01,
            R.raw.victory_celebration_02,
            R.raw.waiting_a_few_moments_later,
            R.raw.waiting_two_hours_later
        )

        allSfx.forEach { resId ->
            soundPool?.let { pool ->
                val sampleId = pool.load(appContext, resId, 1)
                soundIds[resId] = sampleId
            }
        }
    }

    // ================= SFX Playback =================

    private fun playSfx(resId: Int, volumeMultiplier: Float = 1.0f, pitch: Float = 1.0f): Int {
        if (!isSfxEnabled) return 0
        val sampleId = soundIds[resId] ?: return 0
        val vol = (sfxVolume * volumeMultiplier).coerceIn(0.0f, 1.0f)
        return soundPool?.play(sampleId, vol, vol, 1, 0, pitch) ?: 0
    }

    /**
     * Reproduce el sonido de clic de botón con volumen suave y modulación por presión táctil.
     * @param pressure Presión táctil (0.1f a 1.0f).
     */
    fun playButtonClick(pressure: Float = 0.5f) {
        val normalizedPressure = pressure.coerceIn(0.1f, 1.0f)
        // Reducido a volumen suave y agradable
        val volumeMultiplier = 0.10f + (normalizedPressure * 0.28f)
        val pitch = (0.92f + normalizedPressure * 0.14f).coerceIn(0.85f, 1.15f)
        playSfx(R.raw.pop_double_01, volumeMultiplier, pitch)
    }

    /**
     * Reproduce el sonido de revelado de casilla con sensibilidad a la presión táctil.
     */
    fun playCellReveal(pressure: Float = 0.5f) {
        val normalizedPressure = pressure.coerceIn(0.1f, 1.0f)
        val volumeMultiplier = 0.12f + (normalizedPressure * 0.30f)
        val pitch = (0.94f + normalizedPressure * 0.12f).coerceIn(0.85f, 1.15f)
        playSfx(R.raw.pop_double_01, volumeMultiplier, pitch)
    }

    private var duckJob: Job? = null
    private var isDucked: Boolean = false

    /**
     * Temporarily ducks (reduces) the background music volume during high-impact SFX sequences
     * (like defeat explosions or victory celebrations) and smoothly restores it when done.
     */
    fun duckMusicForDuration(durationMillis: Long, duckRatio: Float = 0.10f) {
        if (!isMusicEnabled || !isAppInForeground) return
        duckJob?.cancel()
        duckJob = audioScope.launch {
            try {
                isDucked = true
                val targetDuckedVol = (musicVolume * duckRatio).coerceIn(0.0f, 1.0f)
                val normalVol = musicVolume

                // Quick smooth fade down (~200ms)
                val fadeSteps = 5
                for (step in 1..fadeSteps) {
                    val currentRatio = 1f - (step.toFloat() / fadeSteps)
                    val vol = targetDuckedVol + (normalVol - targetDuckedVol) * currentRatio
                    try {
                        currentPlayer?.setVolume(vol, vol)
                    } catch (e: Exception) {}
                    delay(40L)
                }
                try {
                    currentPlayer?.setVolume(targetDuckedVol, targetDuckedVol)
                } catch (e: Exception) {}

                // Hold ducked level during sequence
                delay((durationMillis - 600L).coerceAtLeast(100L))

                // Smooth fade back up (~600ms)
                val restoreSteps = 10
                for (step in 1..restoreSteps) {
                    val progress = step.toFloat() / restoreSteps
                    val vol = targetDuckedVol + (musicVolume - targetDuckedVol) * progress
                    try {
                        currentPlayer?.setVolume(vol, vol)
                    } catch (e: Exception) {}
                    delay(60L)
                }
                try {
                    currentPlayer?.setVolume(musicVolume, musicVolume)
                } catch (e: Exception) {}
            } finally {
                isDucked = false
            }
        }
    }

    fun playExplosionSequence() {
        if (!isSfxEnabled) return
        duckMusicForDuration(durationMillis = 4000L, duckRatio = 0.10f)
        audioScope.launch {
            val explosionRes = explosionDeck.next()
            playSfx(explosionRes, 1.0f)

            delay(450L) // Timing between explosion impact and funny trumpet

            val funnyRes = defeatTrumpetDeck.next()
            playSfx(funnyRes, 0.95f)
        }
    }

    private var lastVictoryTimestamp: Long = 0L

    fun playVictorySequence() {
        if (!isSfxEnabled) return
        val now = System.currentTimeMillis()
        if (now - lastVictoryTimestamp < 4000L) {
            return // Prevent duplicate concurrent victory triggers
        }
        lastVictoryTimestamp = now

        duckMusicForDuration(durationMillis = 7500L, duckRatio = 0.05f)
        audioScope.launch {
            playSfx(R.raw.victory_woooow, 1.0f)

            delay(1300L) // Timing just before woooow ends

            val celebrationRes = victoryCelebrationDeck.next()
            playSfx(celebrationRes, 1.0f)
        }
    }

    /**
     * Reproduce un sonido festivo y brillante exclusivo para el desbloqueo de logros.
     */
    fun playAchievementUnlockedSound() {
        if (!isSfxEnabled) return
        audioScope.launch {
            // Doble pop armónico brillante con pitch ascendente
            playSfx(R.raw.pop_double_01, volumeMultiplier = 0.65f, pitch = 1.35f)
            delay(110L)
            playSfx(R.raw.pop_01, volumeMultiplier = 0.85f, pitch = 1.55f)
        }
    }

    /**
     * Reproduce el sonido de apertura de la tarjeta de detalle de logro.
     */
    fun playAchievementCardOpenSound(achievementId: String = "") {
        if (!isSfxEnabled) return
        audioScope.launch {
            playSfx(R.raw.pop_double_01, volumeMultiplier = 0.60f, pitch = 1.42f)
            delay(90L)
            playAchievementThemedSound(achievementId, isIntro = true)
        }
    }

    /**
     * Reproduce el sonido característico y personalizado para cada logro del rancho.
     */
    fun playAchievementThemedSound(achievementId: String, isIntro: Boolean = false) {
        if (!isSfxEnabled) return
        audioScope.launch {
            val volMultiplier = if (isIntro) 0.65f else 0.85f
            when (achievementId) {
                "first_win" -> {
                    playSfx(R.raw.pop_double_01, volMultiplier * 0.9f, 1.28f)
                    delay(120L)
                    playSfx(R.raw.victory_celebration_01, volMultiplier * 0.70f, 1.10f)
                }
                "patron_experto" -> {
                    playSfx(R.raw.victory_celebration_02, volMultiplier * 0.90f, 1.05f)
                }
                "fast_hand" -> {
                    playSfx(R.raw.pop_01, volMultiplier, 1.75f)
                    delay(80L)
                    playSfx(R.raw.pop_double_02, volMultiplier * 0.9f, 1.60f)
                }
                "cazador_iguanas" -> {
                    playSfx(R.raw.pop_01, volMultiplier * 0.85f, 0.85f)
                    delay(110L)
                    playSfx(R.raw.pop_double_01, volMultiplier, 1.45f)
                }
                "minero_veterano" -> {
                    playSfx(R.raw.pop_double_02, volMultiplier, 1.40f)
                    delay(100L)
                    playSfx(R.raw.pop_01, volMultiplier * 0.9f, 1.65f)
                }
                "sin_banderas" -> {
                    playSfx(R.raw.pop_double_01, volMultiplier, 1.50f)
                    delay(120L)
                    playSfx(R.raw.victory_woooow, volMultiplier * 0.75f, 1.15f)
                }
                else -> {
                    playAchievementUnlockedSound()
                }
            }
        }
    }

    /**
     * Reproduce el sonido de cierre de la tarjeta de detalle de logro.
     */
    fun playAchievementCardCloseSound() {
        if (!isSfxEnabled) return
        playSfx(R.raw.pop_01, volumeMultiplier = 0.28f, pitch = 0.88f)
    }

    /**
     * Reproduce un sonido sordo de retroalimentación táctil al intentar interactuar con un logro bloqueado.
     */
    fun playLockedAchievementSound() {
        if (!isSfxEnabled) return
        playSfx(R.raw.pop_01, volumeMultiplier = 0.32f, pitch = 0.60f)
    }

    fun playWaitingSound() {
        if (!isSfxEnabled) return
        val waitingRes = waitingDeck.next()
        playSfx(waitingRes, 1.0f)
    }

    // ================= Soundtrack Management =================

    fun startSoundtrack() {
        hasStartedPlayback = true
        if (currentPlayer != null || !isAppInForeground) return
        playCurrentTrackWithFade(fadeIn = true, isCrescendo = true)
    }

    private fun playCurrentTrackWithFade(fadeIn: Boolean = true, isCrescendo: Boolean = false) {
        if (!isMusicEnabled || !isAppInForeground) return
        try {
            val trackRes = soundtracks[currentTrackIndex]
            val player = MediaPlayer.create(appContext, trackRes).apply {
                isLooping = false
                val startVol = if (fadeIn) 0.0f else musicVolume
                setVolume(startVol, startVol)
                setOnCompletionListener {
                    advanceToNextTrackWithCrossfade()
                }
                start()
            }

            currentPlayer = player

            if (fadeIn) {
                audioScope.launch {
                    val steps = if (isCrescendo) 30 else 20
                    val delayMs = 100L
                    val targetVol = musicVolume
                    for (i in 1..steps) {
                        if (currentPlayer != player) break
                        val linearRatio = i.toFloat() / steps
                        val progress = if (isCrescendo) {
                            Math.pow(linearRatio.toDouble(), 1.5).toFloat()
                        } else {
                            linearRatio
                        }
                        val currentVol = targetVol * progress
                        try {
                            player.setVolume(currentVol, currentVol)
                        } catch (e: Exception) {
                            break
                        }
                        delay(delayMs)
                    }
                    try {
                        player.setVolume(targetVol, targetVol)
                    } catch (e: Exception) {}
                }
            }

            // Schedule crossfade ~4 seconds before track end
            monitorTrackForCrossfade(player)

        } catch (e: Exception) {
            Log.e("SoundManager", "Error starting soundtrack", e)
        }
    }

    private fun monitorTrackForCrossfade(player: MediaPlayer) {
        crossfadeJob?.cancel()
        crossfadeJob = audioScope.launch {
            try {
                while (player.isPlaying) {
                    val duration = player.duration
                    val currentPos = player.currentPosition
                    if (duration > 0 && duration - currentPos <= 3500) {
                        advanceToNextTrackWithCrossfade()
                        break
                    }
                    delay(1000L)
                }
            } catch (e: Exception) {
                // Player might have been released
            }
        }
    }

    @Synchronized
    private fun advanceToNextTrackWithCrossfade() {
        if (!isMusicEnabled || !isAppInForeground || isCrossfading) return
        isCrossfading = true

        val oldPlayer = currentPlayer
        currentTrackIndex = (currentTrackIndex + 1) % soundtracks.size
        val nextTrackRes = soundtracks[currentTrackIndex]

        try {
            val newPlayer = MediaPlayer.create(appContext, nextTrackRes).apply {
                isLooping = false
                setVolume(0.0f, 0.0f)
                setOnCompletionListener {
                    advanceToNextTrackWithCrossfade()
                }
                start()
            }

            currentPlayer = newPlayer
            fadingPlayer = oldPlayer

            audioScope.launch {
                val steps = 25
                val targetVol = if (isDucked) (musicVolume * 0.10f) else musicVolume
                for (i in 1..steps) {
                    val fadeInRatio = i.toFloat() / steps
                    val fadeOutRatio = 1.0f - fadeInRatio

                    try {
                        newPlayer.setVolume(targetVol * fadeInRatio, targetVol * fadeInRatio)
                    } catch (e: Exception) {}

                    try {
                        oldPlayer?.setVolume(targetVol * fadeOutRatio, targetVol * fadeOutRatio)
                    } catch (e: Exception) {}

                    delay(120L)
                }

                try {
                    oldPlayer?.stop()
                    oldPlayer?.release()
                } catch (e: Exception) {}

                if (fadingPlayer == oldPlayer) {
                    fadingPlayer = null
                }
                isCrossfading = false
            }

            monitorTrackForCrossfade(newPlayer)

        } catch (e: Exception) {
            Log.e("SoundManager", "Error in crossfade", e)
            isCrossfading = false
        }
    }

    fun pauseMusic() {
        isAppInForeground = false
        crossfadeJob?.cancel()
        duckJob?.cancel()
        isDucked = false
        try {
            currentPlayer?.pause()
            fadingPlayer?.pause()
        } catch (e: Exception) {
            Log.e("SoundManager", "Error pausing music", e)
        }
    }

    fun resumeMusic() {
        isAppInForeground = true
        duckJob?.cancel()
        isDucked = false
        if (isMusicEnabled && hasStartedPlayback) {
            if (currentPlayer != null) {
                try {
                    currentPlayer?.setVolume(musicVolume, musicVolume)
                    currentPlayer?.start()
                } catch (e: Exception) {
                    playCurrentTrackWithFade(fadeIn = false, isCrescendo = false)
                }
            } else {
                playCurrentTrackWithFade(fadeIn = false, isCrescendo = false)
            }
        }
    }

    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0.0f, 1.0f)
        prefs.edit().putFloat(KEY_MUSIC_VOLUME, musicVolume).apply()
        if (isMusicEnabled && isAppInForeground && !isDucked) {
            try {
                currentPlayer?.setVolume(musicVolume, musicVolume)
            } catch (e: Exception) {}
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply()
        if (enabled) {
            resumeMusic()
        } else {
            duckJob?.cancel()
            isDucked = false
            try {
                currentPlayer?.pause()
                fadingPlayer?.pause()
            } catch (e: Exception) {}
        }
    }

    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0.0f, 1.0f)
        prefs.edit().putFloat(KEY_SFX_VOLUME, sfxVolume).apply()
    }

    fun setSfxEnabled(enabled: Boolean) {
        isSfxEnabled = enabled
        prefs.edit().putBoolean(KEY_SFX_ENABLED, enabled).apply()
    }

    fun release() {
        crossfadeJob?.cancel()
        duckJob?.cancel()
        isDucked = false
        try {
            currentPlayer?.release()
            currentPlayer = null
            fadingPlayer?.release()
            fadingPlayer = null
            soundPool?.release()
            soundPool = null
        } catch (e: Exception) {}
    }

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
