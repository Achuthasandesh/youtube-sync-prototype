package com.android.youtubesyncplayer.nsd

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log

class NsdServerRegistrar(
    private val context: Context,
    private val serviceName: String,
    private val port: Int
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    private val registrationListener = object : NsdManager.RegistrationListener {
        override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
            Log.d("NsdServerRegistrar", "Service Registered: ${serviceInfo.serviceName}")
        }
        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e("NsdServerRegistrar", "Registration failed: $errorCode")
        }
        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
            Log.d("NsdServerRegistrar", "Service Unregistered: ${serviceInfo.serviceName}")
        }
        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e("NsdServerRegistrar", "Unregistration failed: $errorCode")
        }
    }

    fun registerService() {
        val serviceInfo = NsdServiceInfo().apply {
            this.serviceName = "YOUTUBE SYNC SERVICE"
            this.serviceType = "_ws._tcp."
            this.port = this@NsdServerRegistrar.port
        }
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    fun unregisterService() {
        nsdManager.unregisterService(registrationListener)
    }
}
