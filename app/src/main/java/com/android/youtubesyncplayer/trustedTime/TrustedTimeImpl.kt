package com.android.youtubesyncplayer.trustedTime

import android.content.Context
import android.util.Log
import com.google.android.gms.time.TrustedTime
import com.google.android.gms.time.TrustedTimeClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class TrustedTimeImpl(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
): TrustedTimeInterface {
    /**
     * A wrapper class for the TrustedTimeClient that initializes it asynchronously.
     */
    private var trustedTimeClient: TrustedTimeClient? = null

    init {
        initializeAsync()
    }
    /**
     * Initializes the TrustedTimeClient asynchronously.
     */
    private fun initializeAsync() {
        if(trustedTimeClient == null){
            scope.launch {
                try {
                    trustedTimeClient = TrustedTime.createClient(context).await()
                    Log.d(TAG, "TrustedTimeClient initialized successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize TrustedTimeClient", e)
                }
            }
        }
    }

    override fun getClient() = trustedTimeClient

    override fun getCurrentTimeMillis(): Long {
        return trustedTimeClient?.computeCurrentUnixEpochMillis() ?: run {
            Log.w(TAG, "TrustedTimeClient not initialized, falling back to system time")
            System.currentTimeMillis()
        }
    }

    companion object {
        private const val TAG = "TrustedTimeClientWrapper"
    }
}