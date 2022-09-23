package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


private const val GEOFENCE_REQ_ID = ".geofence_id"
private const val DEFAULT_GEO_DURATION = 60 * 60 * 1000.toLong()

class SaveReminderFragment : BaseFragment() {

    companion object {
        val TAG = SaveReminderFragment::class.java.simpleName
        const val ACTION_GEOFENCE_EVENT = "geofence_event"
        const val DEFAULT_GEOFENCE_RADIUS = 1000F
    }

    private lateinit var geoFencingClient: GeofencingClient
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        geoFencingClient = LocationServices.getGeofencingClient(requireActivity())

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value ?: 0.0
            val longitude = _viewModel.longitude.value ?: 0.0

//            DONE: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db

            val reminder = ReminderDataItem(title, description, location, latitude, longitude)
            saveReminder(reminder)

            val geofence = createGeofence(LatLng(latitude, longitude), reminder.id)
            val geofenceRequest = createGeofenceRequest(geofence)

            addGeoFence(geofenceRequest, geofencePendingIntent())
        }
    }

    private fun createGeofence(latLng: LatLng, id: String, radius: Float = DEFAULT_GEOFENCE_RADIUS): Geofence {
        return Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setExpirationDuration(DEFAULT_GEO_DURATION)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
    }

    private fun createGeofenceRequest(geoFence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geoFence)
            .build()
    }

    private fun geofencePendingIntent() : PendingIntent {
        val intent = Intent(activity, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        return PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    @SuppressLint("MissingPermission")
    private fun addGeoFence(geoFencingRequest: GeofencingRequest, geofencePIntent: PendingIntent) {
        geoFencingClient.removeGeofences(geofencePIntent).run {
            addOnCompleteListener {
                geoFencingClient.addGeofences(geoFencingRequest, geofencePIntent).run {
                    addOnSuccessListener {
                        activity?.let {
                            Snackbar.make(requireView(), "geofences_added",
                                Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                    addOnFailureListener {
                        activity?.let {
                            Toast.makeText(
                                activity, R.string.geofences_not_added,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        if ((it.message != null)) {
                            Log.w(TAG, it.message.toString())
                        }
                    }
                }
            }
        }
    }

    private fun saveReminder(reminder: ReminderDataItem) {
        _viewModel.validateAndSaveReminder(reminder)
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
