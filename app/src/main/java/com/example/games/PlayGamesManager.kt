package com.example.games

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.GameDifficulty
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Gestor centralizado de Google Play Games Services v2.
 * Maneja autenticación automática, sincronización de logros y envío de puntajes/victorias a marcadores mundiales.
 */
class PlayGamesManager private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var currentActivity: WeakReference<Activity>? = null

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _playerName = MutableStateFlow<String?>(null)
    val playerName: StateFlow<String?> = _playerName.asStateFlow()

    init {
        try {
            PlayGamesSdk.initialize(appContext)
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo inicializar PlayGamesSdk: ${e.message}")
        }
    }

    fun attachActivity(activity: Activity) {
        currentActivity = WeakReference(activity)
        checkAuthentication(activity)
    }

    fun detachActivity(activity: Activity) {
        if (currentActivity?.get() == activity) {
            currentActivity = null
        }
    }

    private fun resolveActivity(provided: Activity? = null): Activity? = provided ?: currentActivity?.get()

    /**
     * Intenta autenticar silenciosamente al usuario con Google Play Games.
     */
    fun checkAuthentication(activity: Activity? = null) {
        val targetActivity = resolveActivity(activity) ?: return
        try {
            val gamesSignInClient = PlayGames.getGamesSignInClient(targetActivity)
            gamesSignInClient.isAuthenticated.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result.isAuthenticated) {
                    _isAuthenticated.value = true
                    fetchPlayerInfo(targetActivity)
                    Log.d(TAG, "Usuario autenticado en Google Play Games")
                } else {
                    _isAuthenticated.value = false
                    _playerName.value = null
                    Log.d(TAG, "Usuario no autenticado en Google Play Games")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error comprobando autenticación de Google Play Games: ${e.message}")
            _isAuthenticated.value = false
        }
    }

    /**
     * Inicia el flujo interactivo de autenticación en Google Play Games.
     */
    fun signIn(activity: Activity? = null, onComplete: ((Boolean) -> Unit)? = null) {
        val targetActivity = resolveActivity(activity) ?: run {
            onComplete?.invoke(false)
            return
        }
        try {
            val gamesSignInClient = PlayGames.getGamesSignInClient(targetActivity)
            gamesSignInClient.signIn().addOnCompleteListener { task ->
                val success = task.isSuccessful && task.result.isAuthenticated
                _isAuthenticated.value = success
                if (success) {
                    fetchPlayerInfo(targetActivity)
                }
                onComplete?.invoke(success)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando sesión en Google Play Games: ${e.message}")
            onComplete?.invoke(false)
        }
    }

    private fun fetchPlayerInfo(activity: Activity) {
        try {
            PlayGames.getPlayersClient(activity).currentPlayer.addOnSuccessListener { player ->
                _playerName.value = player.displayName
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error obteniendo datos del jugador: ${e.message}")
        }
    }

    // ================= Sincronización de Logros =================

    /**
     * Sincroniza y desbloquea un logro local en Google Play Games.
     */
    fun unlockAchievement(localAchievementId: String, activity: Activity? = null) {
        val targetActivity = resolveActivity(activity) ?: return
        val cloudId = getCloudAchievementId(targetActivity, localAchievementId) ?: return
        try {
            PlayGames.getAchievementsClient(targetActivity).unlock(cloudId)
            Log.d(TAG, "Logro desbloqueado en la nube: $localAchievementId -> $cloudId")
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo sincronizar logro $localAchievementId: ${e.message}")
        }
    }

    /**
     * Incrementa un logro progresivo en Google Play Games.
     */
    fun incrementAchievement(localAchievementId: String, steps: Int = 1, activity: Activity? = null) {
        val targetActivity = resolveActivity(activity) ?: return
        val cloudId = getCloudAchievementId(targetActivity, localAchievementId) ?: return
        try {
            PlayGames.getAchievementsClient(targetActivity).increment(cloudId, steps)
            Log.d(TAG, "Logro incrementado en la nube: $localAchievementId ($steps pasos) -> $cloudId")
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo incrementar logro $localAchievementId: ${e.message}")
        }
    }

    /**
     * Sincroniza todos los logros desbloqueados localmente hacia la nube.
     */
    fun syncAllUnlockedAchievements(unlockedLocalIds: List<String>, activity: Activity? = null) {
        if (!_isAuthenticated.value) return
        scope.launch {
            unlockedLocalIds.forEach { localId ->
                unlockAchievement(localId, activity)
            }
        }
    }

    /**
     * Abre la pantalla/overlay nativa oficial de Logros de Google Play Games.
     */
    fun showAchievementsOverlay(activity: Activity? = null) {
        val targetActivity = resolveActivity(activity) ?: return
        if (!_isAuthenticated.value) {
            Log.d(TAG, "showAchievementsOverlay: usuario no autenticado, iniciando signIn...")
            signIn(targetActivity) { success ->
                if (success) {
                    showAchievementsOverlay(targetActivity)
                }
            }
            return
        }
        try {
            PlayGames.getAchievementsClient(targetActivity).achievementsIntent.addOnSuccessListener { intent ->
                targetActivity.startActivity(intent)
            }.addOnFailureListener { e ->
                Log.w(TAG, "No se pudo abrir overlay de logros: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error abriendo overlay de logros: ${e.message}")
        }
    }

    // ================= Marcadores / Leaderboards =================

    /**
     * Envía el mejor tiempo registrado (en milisegundos) al marcador de Google Play Games correspondiente.
     */
    fun submitBestTime(difficulty: GameDifficulty, timeSeconds: Int, activity: Activity? = null) {
        val targetActivity = resolveActivity(activity) ?: return
        val leaderboardId = getBestTimeLeaderboardId(targetActivity, difficulty) ?: return
        try {
            val timeMillis = timeSeconds.toLong() * 1000L
            PlayGames.getLeaderboardsClient(targetActivity).submitScore(leaderboardId, timeMillis)
            Log.d(TAG, "Mejor tiempo enviado a marcador ($difficulty): $timeSeconds seg ($timeMillis ms) -> $leaderboardId")
        } catch (e: Exception) {
            Log.w(TAG, "Error enviando mejor tiempo a Play Games: ${e.message}")
        }
    }

    /**
     * Envía el conteo acumulado de victorias de un mapa al marcador de Google Play Games.
     */
    fun submitWinCount(difficulty: GameDifficulty, totalWins: Int, activity: Activity? = null) {
        val targetActivity = resolveActivity(activity) ?: return
        val leaderboardId = getWinsLeaderboardId(targetActivity, difficulty) ?: return
        try {
            PlayGames.getLeaderboardsClient(targetActivity).submitScore(leaderboardId, totalWins.toLong())
            Log.d(TAG, "Total victorias enviado a marcador ($difficulty): $totalWins -> $leaderboardId")
        } catch (e: Exception) {
            Log.w(TAG, "Error enviando conteo de victorias a Play Games: ${e.message}")
        }
    }

    /**
     * Abre la pantalla/overlay nativa oficial de Marcadores de Google Play Games.
     */
    fun showAllLeaderboardsOverlay(activity: Activity? = null) {
        val targetActivity = resolveActivity(activity) ?: return
        if (!_isAuthenticated.value) {
            Log.d(TAG, "showAllLeaderboardsOverlay: usuario no autenticado, iniciando signIn...")
            signIn(targetActivity) { success ->
                if (success) {
                    showAllLeaderboardsOverlay(targetActivity)
                }
            }
            return
        }
        try {
            PlayGames.getLeaderboardsClient(targetActivity).allLeaderboardsIntent.addOnSuccessListener { intent ->
                targetActivity.startActivity(intent)
            }.addOnFailureListener { e ->
                Log.w(TAG, "No se pudo abrir overlay de marcadores: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error abriendo overlay de marcadores: ${e.message}")
        }
    }

    // ================= Mapeos de Recursos =================

    private fun getCloudAchievementId(context: Context, localId: String): String? {
        return when (localId) {
            "first_win" -> context.getString(R.string.achievement_primer_espuelazo)
            "patron_experto" -> context.getString(R.string.achievement_patron_experto)
            "fast_hand" -> context.getString(R.string.achievement_rapido_viento)
            "cazador_iguanas" -> context.getString(R.string.achievement_cazador_iguanas)
            else -> null
        }
    }

    private fun getBestTimeLeaderboardId(context: Context, difficulty: GameDifficulty): String? {
        return when (difficulty) {
            GameDifficulty.PRINCIPIANTE -> context.getString(R.string.leaderboard_principiante_tiempo)
            GameDifficulty.INTERMEDIO -> context.getString(R.string.leaderboard_intermedio_tiempo)
            GameDifficulty.EXPERTO -> context.getString(R.string.leaderboard_experto_tiempo)
            GameDifficulty.PERSONALIZADA -> null
        }
    }

    private fun getWinsLeaderboardId(context: Context, difficulty: GameDifficulty): String? {
        return when (difficulty) {
            GameDifficulty.PRINCIPIANTE -> context.getString(R.string.leaderboard_principiante_victorias)
            GameDifficulty.INTERMEDIO -> context.getString(R.string.leaderboard_intermedio_victorias)
            GameDifficulty.EXPERTO -> context.getString(R.string.leaderboard_experto_victorias)
            GameDifficulty.PERSONALIZADA -> null
        }
    }

    companion object {
        private const val TAG = "PlayGamesManager"

        @Volatile
        private var instance: PlayGamesManager? = null

        fun getInstance(context: Context): PlayGamesManager {
            return instance ?: synchronized(this) {
                instance ?: PlayGamesManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
