package com.example

import android.app.Application
import android.util.Log
import com.google.android.gms.games.PlayGamesSdk

/**
 * Clase de aplicación global para Buscaminas Del Rancho.
 * Responsable de la inicialización temprana del SDK de Google Play Games Services v2
 * para garantizar el correcto enganche de callbacks del ciclo de vida de Activities.
 */
class RanchoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            PlayGamesSdk.initialize(this)
            Log.d(TAG, "PlayGamesSdk inicializado exitosamente en RanchoApplication.onCreate()")
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando PlayGamesSdk en RanchoApplication: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "RanchoApplication"
    }
}

