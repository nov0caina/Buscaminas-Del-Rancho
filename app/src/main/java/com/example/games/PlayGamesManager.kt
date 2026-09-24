package com.example.games

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.R
import com.example.data.model.GameDifficulty
import com.example.data.repository.GlobalLeaderboardEntry
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Gestor centralizado de Google Play Games Services v2.
 * Maneja autenticación automática, sincronización de logros y consulta/envío de puntuaciones en tiempo real.
 */
class PlayGamesManager private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var currentActivity: WeakReference<Activity>? = null

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _playerName = MutableStateFlow<String?>(null)
    val playerName: StateFlow<String?> = _playerName.asStateFlow()

    private val _lastAuthError = MutableStateFlow<String?>(null)
    val lastAuthError: StateFlow<String?> = _lastAuthError.asStateFlow()

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

    private fun formatAuthError(exception: Throwable?): String {
        if (exception == null) return "Error desconocido (sin excepción)"
        if (exception is com.google.android.gms.common.api.ApiException) {
            val code = exception.statusCode
            val codeName = when (code) {
                4 -> "SIGN_IN_REQUIRED (4: Se requiere abrir la app Play Juegos)"
                7 -> "NETWORK_ERROR (7: Error de conexión)"
                8 -> "INTERNAL_ERROR (8: Error interno de Google)"
                10 -> "DEVELOPER_ERROR (10: SHA-1 o paquete no coincide en Google Cloud)"
                13 -> "ERROR (13: Error general)"
                14 -> "INTERRUPTED (14: Interrumpido)"
                15 -> "TIMEOUT (15: Tiempo de espera agotado)"
                16 -> "CANCELED (16: Sin permisos de tester en Play Games)"
                17 -> "API_NOT_CONNECTED (17: API no conectada)"
                12501 -> "SIGN_IN_CANCELLED (12501: Cancelado por el usuario)"
                12502 -> "SIGN_IN_CURRENTLY_IN_PROGRESS (12502: En progreso)"
                else -> "Código $code"
            }
            return "$codeName: ${exception.message ?: ""}"
        }
        return exception.message ?: exception.javaClass.simpleName
    }

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
                    _lastAuthError.value = null
                    fetchPlayerInfo(targetActivity)
                    Log.d(TAG, "Usuario autenticado silenciosamente en Google Play Games")
                } else {
                    _isAuthenticated.value = false
                    _playerName.value = null
                    val err = if (!task.isSuccessful) formatAuthError(task.exception) else "Sesión no activa"
                    _lastAuthError.value = err
                    Log.d(TAG, "Usuario no autenticado silenciosamente: $err")
                }
            }
        } catch (e: Exception) {
            val err = "Error comprobando autenticación: ${e.message}"
            _lastAuthError.value = err
            Log.w(TAG, err)
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
                if (task.isSuccessful) {
                    val result = task.result
                    val success = result.isAuthenticated
                    _isAuthenticated.value = success
                    if (success) {
                        _lastAuthError.value = null
                        fetchPlayerInfo(targetActivity)
                        Log.d(TAG, "Inicio de sesión exitoso en Google Play Games")
                    } else {
                        val err = "Autenticación no autorizada por Google (isAuthenticated=false). Verifica ser evaluador en Play Games Services."
                        _lastAuthError.value = err
                        Log.w(TAG, err)
                        Log.w(TAG, "signIn: Google retornó isAuthenticated=false. Causas probables: 1) Cuenta de Google no agregada en 'Play Games Services -> Evaluadores', 2) App ejecutada localmente con firma distinta a Play App Signing sin cliente OAuth correspondiente en Google Cloud, 3) Cambios en Play Games Services pendientes de publicar.")
                    }
                    onComplete?.invoke(success)
                } else {
                    val err = formatAuthError(task.exception)
                    _lastAuthError.value = err
                    _isAuthenticated.value = false
                    Log.e(TAG, "Error en signIn: $err", task.exception)
                    onComplete?.invoke(false)
                }
            }
        } catch (e: Exception) {
            val err = "Error iniciando sesión: ${e.message}"
            _lastAuthError.value = err
            Log.e(TAG, err, e)
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
                } else {
                    val detail = _lastAuthError.value ?: "Error al autenticar"
                    Toast.makeText(
                        targetActivity,
                        "Google Play Games: $detail",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            return
        }
        try {
            PlayGames.getAchievementsClient(targetActivity).achievementsIntent
                .addOnSuccessListener { intent ->
                    Log.d(TAG, "showAchievementsOverlay: lanzando pantalla oficial de logros")
                    targetActivity.startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "No se pudo abrir overlay de logros: ${e.message}", e)
                    Toast.makeText(
                        targetActivity,
                        "Los logros se están sincronizando con Google Play...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error abriendo overlay de logros: ${e.message}", e)
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
     * Carga las puntuaciones globales reales desde Google Play Games.
     */
    fun fetchLiveLeaderboard(
        difficulty: GameDifficulty,
        isTimeMetric: Boolean,
        activity: Activity? = null,
        onLoaded: (List<GlobalLeaderboardEntry>) -> Unit
    ) {
        val targetActivity = resolveActivity(activity) ?: run {
            onLoaded(emptyList())
            return
        }
        val leaderboardId = if (isTimeMetric) {
            getBestTimeLeaderboardId(targetActivity, difficulty)
        } else {
            getWinsLeaderboardId(targetActivity, difficulty)
        } ?: run {
            onLoaded(emptyList())
            return
        }

        try {
            PlayGames.getLeaderboardsClient(targetActivity)
                .loadTopScores(leaderboardId, LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC, 25)
                .addOnSuccessListener { annotatedData ->
                    val buffer = annotatedData.get()?.scores
                    val results = mutableListOf<GlobalLeaderboardEntry>()
                    if (buffer != null) {
                        for (i in 0 until buffer.count) {
                            val score = buffer.get(i)
                            val rank = score.rank.toInt()
                            val name = score.scoreHolderDisplayName
                            val rawScore = score.rawScore
                            results.add(
                                GlobalLeaderboardEntry(
                                    rank = rank,
                                    playerName = name,
                                    location = "México 🇲🇽",
                                    timeSeconds = if (isTimeMetric) (rawScore / 1000).toInt() else 0,
                                    winCount = if (!isTimeMetric) rawScore.toInt() else 0,
                                    difficulty = difficulty.displayName,
                                    avatarEmoji = if (rank == 1) "👑" else if (rank <= 3) "🥇" else "🤠"
                                )
                            )
                        }
                        buffer.release()
                    }
                    Log.d(TAG, "Marcadores reales cargados desde Play Games: ${results.size} registros ($difficulty, isTime: $isTimeMetric)")
                    onLoaded(results)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "No se pudieron cargar marcadores reales de Google Play Games: ${e.message}")
                    onLoaded(emptyList())
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando marcadores reales: ${e.message}")
            onLoaded(emptyList())
        }
    }

    /**
     * Abre la pantalla/overlay nativa oficial de Marcadores de Google Play Games.
     */
    fun showLeaderboardOverlay(
        difficulty: GameDifficulty,
        isTimeMetric: Boolean,
        activity: Activity? = null
    ) {
        val targetActivity = resolveActivity(activity) ?: return
        val leaderboardId = if (isTimeMetric) {
            getBestTimeLeaderboardId(targetActivity, difficulty)
        } else {
            getWinsLeaderboardId(targetActivity, difficulty)
        }

        if (!_isAuthenticated.value) {
            Log.d(TAG, "showLeaderboardOverlay: usuario no autenticado, iniciando signIn...")
            signIn(targetActivity) { success ->
                if (success) {
                    showLeaderboardOverlay(difficulty, isTimeMetric, targetActivity)
                } else {
                    val detail = _lastAuthError.value ?: "Error al autenticar"
                    Toast.makeText(
                        targetActivity,
                        "Google Play Games: $detail",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            return
        }

        try {
            if (leaderboardId != null) {
                PlayGames.getLeaderboardsClient(targetActivity).getLeaderboardIntent(leaderboardId)
                    .addOnSuccessListener { intent ->
                        Log.d(TAG, "Lanzando overlay de marcador específico ($leaderboardId)")
                        targetActivity.startActivity(intent)
                    }
                    .addOnFailureListener {
                        showAllLeaderboardsOverlay(targetActivity)
                    }
            } else {
                showAllLeaderboardsOverlay(targetActivity)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error abriendo overlay de marcador: ${e.message}", e)
            showAllLeaderboardsOverlay(targetActivity)
        }
    }

    /**
     * Abre el panel de todos los marcadores en Google Play Games.
     */
    fun showAllLeaderboardsOverlay(activity: Activity? = null) {
        val targetActivity = resolveActivity(activity) ?: return
        if (!_isAuthenticated.value) {
            signIn(targetActivity) { success ->
                if (success) {
                    showAllLeaderboardsOverlay(targetActivity)
                }
            }
            return
        }
        try {
            PlayGames.getLeaderboardsClient(targetActivity).allLeaderboardsIntent
                .addOnSuccessListener { intent ->
                    Log.d(TAG, "showAllLeaderboardsOverlay: lanzando pantalla oficial de marcadores")
                    targetActivity.startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "No se pudo abrir overlay de marcadores: ${e.message}", e)
                    Toast.makeText(
                        targetActivity,
                        "Los marcadores se están sincronizando con Google Play...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error abriendo overlay de marcadores: ${e.message}", e)
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
