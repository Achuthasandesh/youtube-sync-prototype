package com.android.youtubesyncplayer.trustedTime

import com.google.android.gms.time.TrustedTimeClient

interface TrustedTimeInterface {
    /**
     * Gets the TrustedTimeClient if initialized.
     * @return Initialized TrustedTimeClient or null
     */
    fun getClient(): TrustedTimeClient?

    /**
     * Gets the current time in milliseconds if the client is initialized.
     * @return Current time in milliseconds or fallback to system time if client isn't initialized
     */
    fun getCurrentTimeMillis(): Long

}