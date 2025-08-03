package com.android.youtubesyncplayer.nsd

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class NsdDiscoveryHelper(
    private val context: Context,
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    private val _discoveredServices = mutableStateListOf<NsdServiceInfo>()

    val discoveredServices: SnapshotStateList<NsdServiceInfo> = _discoveredServices

    private val serviceType = "_ws._tcp."
    private var discoveryListener: NsdManager.DiscoveryListener? = null



    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Log failure if needed
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            // Add to list if not already present
            if (_discoveredServices.none { it.serviceName == serviceInfo.serviceName }) {
                _discoveredServices.add(serviceInfo)
            }
        }
    }

    fun startDiscovery() {
        try {
            discoveryListener?.let { nsdManager.stopServiceDiscovery(it) }
            discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(serviceType: String) {
                    // Optional: log or indicate discovery started
                }

                override fun onServiceFound(service: NsdServiceInfo) {
                    if (service.serviceType == this@NsdDiscoveryHelper.serviceType) {
                        nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) { }
                            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                                if (_discoveredServices.none { it.serviceName == serviceInfo.serviceName }) {
                                    _discoveredServices.add(serviceInfo)
                                }
                            }
                        })
                    }
                }

                override fun onServiceLost(service: NsdServiceInfo) {
                    _discoveredServices.removeAll { it.serviceName == service.serviceName }
                }

                override fun onDiscoveryStopped(serviceType: String) {
                    // Optional: log or clean up
                }

                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    // Optional: handle error
                    nsdManager.stopServiceDiscovery(this)
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                    nsdManager.stopServiceDiscovery(this)
                }
            }
            nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            // Handle exceptions, e.g., discovery already started
        }
    }

    fun stopDiscovery() {
        try {
            nsdManager.stopServiceDiscovery(discoveryListener)
        } catch (e: Exception) {
            // Handle exceptions
        }
    }
}
