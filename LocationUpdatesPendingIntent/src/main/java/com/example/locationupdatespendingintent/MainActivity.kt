package com.example.locationupdatespendingintent

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar

/**
 * The only activity in this sample. Displays UI widgets for requesting and removing location
 * updates, and for the batched location updates that are reported.
 *
 * "Q" supports three user choices for location:
 * <ul>
 *     <li>Allow all the time</li>
 *     <li>Allow while app is in use, i.e., while app is in foreground</li>
 *     <li>Not allow location</li>
 * </ul>
 *
 * Because this app requires location updates while the app isn't in use to work, i.e., not in the
 * foreground, the app requires the users to approve "all the time" for location access.
 *
 * However, best practice is to handle "all the time" and "while in use" permissions via a
 * foreground service + Notification. This use case is shown in the
 * LocationUpdatesForegroundService sample in this same repo.
 *
 * We still wanted to show an example where the app needs location access "all the time" for its
 * location features to be enabled (this sample).
 *
 * Location updates requested through this activity continue even when the activity is not in the
 * foreground. Note: apps running on "O" devices (regardless of targetSdkVersion) may receive
 * updates less frequently than the interval specified in the {@link LocationRequest} when the app
 * is no longer in the foreground.
 */
class MainActivity : AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val TAG: String =
        MainActivity::class.java.getSimpleName()
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private val UPDATE_INTERVAL: Long = 60000 // Every 60 seconds.


    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    private val FASTEST_UPDATE_INTERVAL: Long = 30000 // Every 30 seconds


    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private val MAX_WAIT_TIME = UPDATE_INTERVAL * 5 // Every 5 minutes.


    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private var mLocationRequest: LocationRequest? = null

    /**
     * Provides access to the Fused Location Provider API.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    // UI Widgets.
    private var mRequestUpdatesButton: Button? = null
    private var mRemoveUpdatesButton: Button? = null
    private var mLocationUpdatesResultView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRequestUpdatesButton = findViewById(R.id.request_updates_button)
        mRemoveUpdatesButton = findViewById(R.id.remove_updates_button)
        mLocationUpdatesResultView = findViewById(R.id.location_updates_result)

        // Check if the user revoked runtime permissions.
        if(!checkPermissions()){
            requestPermissions()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        updateButtonsState(Utils.getRequestingLocationUpdates(this))
        mLocationUpdatesResultView!!.setText(Utils.getLocationUpdatesResult(this))
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private fun createLocationRequest(){
        mLocationRequest = LocationRequest()

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.
        mLocationRequest!!.setInterval(UPDATE_INTERVAL)

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest!!.setFastestInterval(FASTEST_UPDATE_INTERVAL)

        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest!!.setMaxWaitTime(MAX_WAIT_TIME)
    }

    private fun getPendingIntent(): PendingIntent{
        // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".

        // TODO(developer): uncomment to use PendingIntent.getService().
//        Intent intent = new Intent(this, LocationUpdatesIntentService.class);
//        intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
//        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES)
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Return the current state of the permissions needed.
     */

    private fun checkPermissions(): Boolean{
        val fineLocationPermissionState = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION)

        val backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        return (fineLocationPermissionState == PackageManager.PERMISSION_GRANTED) &&
                (backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions(){
        val shouldProvideRationale = checkPermissions()

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale){
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                findViewById(R.id.activity_main),
                R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, object: View.OnClickListener{
                    override fun onClick(v: View?) {
                        // Request permission
                        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
                    }

                }).show()
        }else{
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if(requestCode == REQUEST_PERMISSIONS_REQUEST_CODE){
            if(grantResults.size <= 0){

            }else if((grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
                (grantResults[1] == PackageManager.PERMISSION_GRANTED)){
                // Permission was granted.
                requestLocationUpdates(null)
            }else{
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.settings, object: View.OnClickListener{
                        override fun onClick(v: View?) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            intent.setData(uri)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }

                    }).show()
            }
        }
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key!!.equals(Utils.KEY_LOCATION_UPDATES_RESULT)){
            mLocationUpdatesResultView!!.setText(Utils.getLocationUpdatesResult(this))
        }else if(key.equals(Utils.KEY_LOCATION_UPDATES_REQUESTED)){
            updateButtonsState(Utils.getRequestingLocationUpdates(this))
        }
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    fun requestLocationUpdates(view: View?){
        try {
            Log.i(TAG, "Starting location updates")
            Utils.setRequestingLocationUpdates(this, true)
            mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, getPendingIntent())
        }catch (e: SecurityException){
            Utils.setRequestingLocationUpdates(this, false)
            e.printStackTrace()
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    fun removeLocationUpdates(view: View?){
        Log.i(TAG, "Removing location updates")
        Utils.setRequestingLocationUpdates(this, false)
        mFusedLocationClient!!.removeLocationUpdates(getPendingIntent())
    }

    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
    private fun updateButtonsState(state: Boolean){
        if(state){
            mRequestUpdatesButton!!.isEnabled = false
            mRemoveUpdatesButton!!.isEnabled = true
        }else{
            mRequestUpdatesButton!!.isEnabled = true
            mRemoveUpdatesButton!!.isEnabled = false
        }
    }
}