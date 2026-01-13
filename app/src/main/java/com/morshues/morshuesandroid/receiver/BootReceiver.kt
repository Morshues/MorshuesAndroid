package com.morshues.morshuesandroid.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.morshues.morshuesandroid.data.sync.PeriodicSyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * BroadcastReceiver that listens for device boot completion.
 * Re-initializes periodic sync workers after device reboot.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var periodicSyncScheduler: PeriodicSyncScheduler

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device boot completed, re-initializing periodic sync")
            periodicSyncScheduler.scheduleAll()
        }
    }
}
