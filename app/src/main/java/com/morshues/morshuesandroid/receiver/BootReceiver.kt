package com.morshues.morshuesandroid.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.morshues.morshuesandroid.data.sync.PeriodicSyncScheduler

/**
 * BroadcastReceiver that listens for device boot completion.
 * Re-initializes periodic sync workers after device reboot.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device boot completed, re-initializing periodic sync")
            PeriodicSyncScheduler.init(context)
        }
    }
}
