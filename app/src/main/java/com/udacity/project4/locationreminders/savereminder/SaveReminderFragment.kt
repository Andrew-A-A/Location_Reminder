package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject



val androidQ=(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
val androidR_Plus=(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
class SaveReminderFragment : BaseFragment() {
    private lateinit var permissionsSnackBarQ:Snackbar
    private var reminder=ReminderDataItem("","","",0.0,0.0)
    //private lateinit var activity:Activity
    companion object {
        const val TAG = "SaveReminderFragment"
        const val ACTION_GEOFENCE_EVENT = "geofence_event"
        const val DEFAULT_GEOFENCE_RADIUS = 120F
    }

    private lateinit var geoFencingClient: GeofencingClient
    //Get the view model in a local variable to access data
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

       // if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
       // ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
        setDisplayHomeAsUpEnabled(true)



        binding.viewModel = _viewModel

        return binding.root
    }

    @SuppressLint("NewApi")
    private fun checkGpsAndSave() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null,0,0,0,null)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                //Create a geofence request and save it
                if (!androidQ)
                    saveButtonOnClickListnerLowerQ()
                else {
                    saveButtonOnClickListenerQ()
                }
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = this.activity
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }


            permissionsSnackBarQ=makePermissionSnackBarQ()
        geoFencingClient = LocationServices.getGeofencingClient(requireActivity())

        binding.saveReminder.setOnClickListener {
            checkGpsAndSave()

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveButtonOnClickListenerQ() {
        if (isBackgroundPermissionGranted()) {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value ?: 0.0
            val longitude = _viewModel.longitude.value ?: 0.0

            //Create reminder and save it
             reminder =
                ReminderDataItem(title, description, location, latitude, longitude)
            saveReminder(reminder)

            //Create a geofence request and save it
            val geofence = createGeofence(LatLng(latitude, longitude), reminder.id)
            val geofenceRequest = createGeofenceRequest(geofence)

          addGeoFence(geofenceRequest, geofencePendingIntent())


        } else {
            requestBackgroundPermission()
            if (!isBackgroundPermissionGranted())
                permissionsSnackBarQ.show()
        }
    }

    private fun saveButtonOnClickListnerLowerQ() {

        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value ?: 0.0
        val longitude = _viewModel.longitude.value ?: 0.0

        //Create reminder and save it
        val reminder =
            ReminderDataItem(title, description, location, latitude, longitude)
        saveReminder(reminder)

        //Create a geofence request and save it
        val geofence = createGeofence(LatLng(latitude, longitude), reminder.id)
        val geofenceRequest = createGeofenceRequest(geofence)

        addGeoFence(geofenceRequest, geofencePendingIntent())
    }

    private fun createGeofence(latLng: LatLng, id: String, radius: Float = DEFAULT_GEOFENCE_RADIUS): Geofence {
        return Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
    }

    private fun createGeofenceRequest(geoFence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geoFence)
            .build()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun geofencePendingIntent() : PendingIntent {
        val intent = Intent(activity, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        return PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    @SuppressLint("MissingPermission")
    private fun addGeoFence(geoFencingRequest: GeofencingRequest, geofencePIntent: PendingIntent) {
        geoFencingClient.addGeofences(geoFencingRequest,geofencePIntent).run {
            addOnCompleteListener {
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

    private fun saveReminder(reminder: ReminderDataItem) {
        _viewModel.validateAndSaveReminder(reminder)
        _viewModel.onClear()
    }
    //Check if background location permission is granted
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isBackgroundPermissionGranted(): Boolean{
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundPermission() {
        var requests= arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if (!isForegroundPermissionGranted()) {
            requests += Manifest.permission.ACCESS_FINE_LOCATION
        }
            ActivityCompat.requestPermissions(
                requireActivity(),
                requests,
                REQUEST_LOCATION_PERMISSION
            )
    }

    //Check if Location Permissions are granted
    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    private fun isForegroundPermissionGranted(): Boolean{
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (
//            grantResults.isEmpty() ||
//            grantResults[2] == PackageManager.PERMISSION_DENIED ||
//            (requestCode == REQUEST_LOCATION_PERMISSION &&
//                    grantResults[1] ==
//                    PackageManager.PERMISSION_DENIED)){
//            // Permission denied.
//            Snackbar.make(
//                requireView(),
//                R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
//            )
//                .setAction(R.string.settings) {
//                    // Displays App settings screen.
//                    startActivity(Intent().apply {
//                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
//                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    })
//                }.show()
//        }
//    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        if (isBackgroundPermissionGranted())
            permissionsSnackBarQ.dismiss()
    }
    private fun makePermissionSnackBarQ() = Snackbar.make(
        requireView(),
        R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
    )
        //Set button to open settings
        .setAction(R.string.settings) {
            // Displays App settings screen.
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }

    override fun onDestroy() {
        super.onDestroy()
        permissionsSnackBarQ.dismiss()
    }
}
private const val  REQUEST_LOCATION_PERMISSION=1
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
