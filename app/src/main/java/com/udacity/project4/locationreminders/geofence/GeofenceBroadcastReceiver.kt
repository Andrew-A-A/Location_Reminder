package com.udacity.project4.locationreminders.geofence


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */
private const val TAG = "GeofenceReceiver"
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        val TAG = "lol"
    }

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == SaveReminderFragment.ACTION_GEOFENCE_EVENT) {
            val geoFencingEvent = GeofencingEvent.fromIntent(intent)

            if (geoFencingEvent!!.hasError()) {
                val errorMessage ="Fencing Error"
                Log.e(TAG, errorMessage)
                return
            }

            if (geoFencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.v(TAG, context.getString(R.string.geofence_entered))
                when {
                    geoFencingEvent.triggeringGeofences!!.isNotEmpty() ->
                        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
                    else -> {
                        Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                        return
                    }
                }
            }
        }

    }

    private fun getErrorString(errorCode: Int): String? {
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "GeoFence not available"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Too many GeoFences"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "Too many pending intents"
            else -> "Unknown error."
        }
    }
}