package com.example.locationupdatespendingintent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationResult

/**
 * Receiver for handling location updates.
 *
 * For apps targeting API level O
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} should be used when
 * requesting location updates. Due to limits on background services,
 * {@link android.app.PendingIntent#getService(Context, int, Intent, int)} should not be used.
 *
 *  Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 *  less frequently than the interval specified in the
 *  {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 *  foreground.
 */
class LocationUpdatesBroadcastReceiver: BroadcastReceiver() {
    private val TAG = "LUBroadcastReceiver"

    companion object{
        val ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.locationupdatespendingintent.action" +
                    ".PROCESS_UPDATES"
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null){
            val action = intent.action
            if(ACTION_PROCESS_UPDATES.equals(action)){
                val result = LocationResult.extractResult(intent)
                if(result != null){
                    val locations = result.locations
                    Utils.setLocationUpdatesResult(context!!, locations)
                    Utils.sendNotification(context, Utils.getLocationResultTitle(context, locations))
                    Log.i(TAG, Utils.getLocationUpdatesResult(context)!!)
                }
            }

        }
    }
}