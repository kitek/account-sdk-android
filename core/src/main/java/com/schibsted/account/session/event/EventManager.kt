/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.session.event

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.schibsted.account.common.util.Logger
import com.schibsted.account.common.util.existsOnClasspath

/**
 * This class is responsible for broadcasting events. This will inspect the classpath to see if the
 * support library is available and only broadcast if it is. You should keep a reference to this
 * class in the main [android.app.Application].
 * @param appContext The application's context, not the application itself
 */
class EventManager(appContext: Context) {
    init {
        if (existsOnClasspath("android.support.v4.content.LocalBroadcastManager")) {
            Logger.info(TAG, { "Android support library v4 found on classpath, will broadcast events" })
            EventManager.broadcastManager = BroadcastManager(appContext)
        } else {
            Logger.info(TAG, { "Android support library v4 not found on classpath, will not broadcast events" })
        }
    }

    fun registerReceiver(broadcastReceiver: BroadcastReceiver, intentFilter: IntentFilter) {
        Logger.verbose(TAG, {
            "register : ${broadcastReceiver.javaClass.simpleName} " +
                if (broadcastManager == null) "but no instance of broadcastManager is found" else "to the broadcast manager"
        })
        broadcastManager?.registerReceiver(broadcastReceiver, intentFilter)
    }

    fun unregisterReceiver(broadcastReceiver: BroadcastReceiver) {
        broadcastManager?.unregisterReceiver(broadcastReceiver)
    }

    companion object {
        private val TAG = Logger.DEFAULT_TAG + "-BCAST"
        private var broadcastManager: BroadcastManager? = null

        @JvmField
        val LOGOUT_EVENT_ID = "IdentityLogoutEvent"

        @JvmField
        val REFRESH_EVENT_ID = "IdentityRefreshEvent"

        @JvmField
        val EXTRA_USER_ID = "userId"

        private fun createIntent(event: BroadcastEvent): Intent = when (event) {
            is BroadcastEvent.LogoutEvent -> Intent(LOGOUT_EVENT_ID).apply { putExtra(EXTRA_USER_ID, event.userId) }
            is BroadcastEvent.RefreshEvent -> Intent(REFRESH_EVENT_ID).apply { putExtra(EXTRA_USER_ID, event.userId) }
        }

        internal fun broadcast(event: BroadcastEvent) {
            Logger.verbose(TAG, {
                "Broadcasting event: ${event.javaClass.simpleName} " +
                    if (broadcastManager == null) "but no instance exists" else "to real broadcast manager"
            })
            EventManager.broadcastManager?.broadcast(createIntent(event))
        }
    }
}