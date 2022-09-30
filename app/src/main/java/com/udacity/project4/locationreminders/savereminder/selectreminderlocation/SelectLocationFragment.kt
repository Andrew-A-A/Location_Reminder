package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


private const val  REQUEST_LOCATION_PERMISSION=1

class SelectLocationFragment : BaseFragment(),OnMapReadyCallback {
    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    //Flag to detect if map is ready or not
    private var isMapReady=false

    //fused Location Client will be used to get last detected location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Flag to detect if a marker added to the map
    private var isMarkerAdded=false

    //Binding variable
    private lateinit var binding: FragmentSelectLocationBinding

    //Dialog for user to confirm selected location
    private lateinit var confirmDialog:AlertDialog

    //Map variable
    private lateinit var map:GoogleMap

    //Selected location
    private lateinit var selectedPOI: PointOfInterest

    //Snack bar for android SDK >= 11
    private lateinit var permissionsSnackBar_R_plus:Snackbar

    //Snack bar for android SDK>= 12
    private lateinit var permissionsSnackBarQ:Snackbar



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        val mapFragment= childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        fusedLocationClient=LocationServices.getFusedLocationProviderClient(requireActivity())
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        binding.saveBtn.setOnClickListener {
            if (isMarkerAdded)
                onLocationSelected()
            else {
                Snackbar.make(requireView(), R.string.please_select_location, Snackbar.LENGTH_LONG).show()
                Log.e(TAG,"No Location")
            }
        }
        if (!isForegroundPermissionGranted())
            requestForegroundPermission()


        return binding.root
    }

    private fun requestForegroundPermission() {
        ActivityCompat.requestPermissions(
            this.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    //Called when user press Save button
    private fun onLocationSelected() {
        confirmDialog = createConfirmDialog()
        //Show Confirm Dialog
        confirmDialog.show()
    }
  //Build the Confirm dialog
    private fun createConfirmDialog(): AlertDialog {
        val confirmDialogBuilder = AlertDialog.Builder(context).apply {
            setTitle(R.string.app_name)
            setMessage("Save selected location ?")
            setIcon(R.drawable.icauncherforeground)
            setPositiveButton("Yes") { dialog, id ->
                Snackbar.make(requireView(), "Location Saved", Snackbar.LENGTH_SHORT).show()
                _viewModel.latitude.value = selectedPOI.latLng.latitude
                _viewModel.longitude.value = selectedPOI.latLng.longitude
                _viewModel.reminderSelectedLocationStr.value = selectedPOI.name
                _viewModel.selectedPOI.value = selectedPOI
                _viewModel.navigationCommand.value=NavigationCommand.Back
                dialog.dismiss()
            }
            setNegativeButton(
                "No"
            ) { dialog, id ->
                dialog.dismiss()
            }
        }
        return confirmDialogBuilder.create()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    // Called when user select item from options menu to change map style
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType=GoogleMap.MAP_TYPE_NORMAL
            requestForegroundPermission()
            true
        }
        R.id.hybrid_map -> {
            map.mapType=GoogleMap.MAP_TYPE_HYBRID
            requestForegroundPermission()
            true
        }
        R.id.satellite_map -> {
            map.mapType=GoogleMap.MAP_TYPE_SATELLITE
            requestForegroundPermission()
            true
        }
        R.id.terrain_map -> {
            map.mapType=GoogleMap.MAP_TYPE_TERRAIN
            requestForegroundPermission()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    //Called when map is ready
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        isMapReady=true
        map = googleMap

        //Set map Style
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),R.raw.map_style))
        //Add the "long click to add marker" behavior to the map
       setMapLongClick(map)
        //Add the "One tap to select Point of Interest" behavior to the map
        setPoiClick(map)

        //Request permissions
           if (isForegroundPermissionGranted())
               map.isMyLocationEnabled=true


        if (isForegroundPermissionGranted())
        //Zoom to last detected location
        fusedLocationClient.lastLocation.addOnSuccessListener { location:Location? ->
            if (location!=null){
                val zoomLevel=16f
                val latLng=LatLng(location.latitude,location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoomLevel))
            }
        }

    }


    //Check if Location Permissions are granted
    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    private fun isForegroundPermissionGranted(): Boolean{
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }




    //Ask user for permissions depending on android SDK version
//    @SuppressLint("InlinedApi", "MissingPermission", "NewApi")
//    private fun enableMyLocation(){
//        if(isForegroundPermissionGranted()){
//            if (androidQ) {
//                if (!isBackgroundPermissionGranted())
//                    requestBackgroundPermission()
//            }
//            map.isMyLocationEnabled=true
//        }
//        else{
//            // Permission denied or not given yet
//
//            //Ask to go to settings and allow location permission
//            permissionsSnackBarQ.show()
//
//            //Foreground Location permission
//            val req=   arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION)
//
//                ActivityCompat.requestPermissions(
//                    requireActivity(),
//                    req,
//                    REQUEST_LOCATION_PERMISSION
//                )
//
//     //If Android SDK is >= 11 then the User has to go to setting and allow the background permission
//            // Source: https://developer.android.com/training/location/permissions#background
//    //If Android SDK is == 10 another window will appear to ask user to allow background location
//
//            if (androidR_Plus || androidQ) {
//                requestBackgroundPermission()
//            }
//
//        }
//        //Show Snack bar to tell user to Allow background location permission
//        //in case if android sdk is >= 11
//        if (androidR_Plus) {
//            if (!isBackgroundPermissionGranted() && isForegroundPermissionGranted()) {
//                requestBackgroundPermission()
//                permissionsSnackBar_R_plus.show()
//            }
//        }
//
//    }



    //Function attach Point of Interest click listener to a given map object
    private fun setPoiClick(map: GoogleMap){
        map.setOnPoiClickListener{poi->
            if (isMarkerAdded)
            map.clear()
            val poiMarker=map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
            isMarkerAdded=true
            selectedPOI=poi
            map.addCircle(CircleOptions().center(poi.latLng).radius(120.0))
        }
    }
    //Function attach long click listener on a given map object
    private fun setMapLongClick(map:GoogleMap){
        map.setOnMapLongClickListener {
            //A Snippet is Additional text that's displayed below the title.
            val snippet=String.format(
                Locale.getDefault(),
                "Lat: %1$.5f , Long: %2$.5f",
                it.latitude,
                it.longitude
            )
            //Clear last marker added
            if (isMarkerAdded) {
                map.clear()
            }
            //Add new marker
            val marker=map.addMarker(
                MarkerOptions().position(it)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )
            //Add circle around marker to represent the geofence
            map.addCircle(CircleOptions().center(it).radius(120.0))
            if (marker!=null)
            selectedPOI= PointOfInterest(marker.position,marker.title.toString(),marker.title.toString())
            isMarkerAdded=true
        }
    }

    //Build Snack bar that asks user to allow location permission for Android SDK >= 11
    private fun makePermissionsSnackBarR() = Snackbar.make(
        requireView(),
        "'All The time' Location needed to set Geofence", Snackbar.LENGTH_INDEFINITE
    )
        .setAction(R.string.settings) {
            // Displays App settings screen.
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }

    //Build Snack bar that asks user to allow location permission for Android SDK == 11
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

    @SuppressLint("NewApi", "MissingPermission")
    override fun onResume() {
        super.onResume()
        //Move Camera to last known location
        if (isMapReady&&isForegroundPermissionGranted()) {
            map.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location:Location? ->
                if (location!=null){
                    val zoomLevel=16f
                    val latLng=LatLng(location.latitude,location.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoomLevel))
                }
            }
        }
        //Confirm that foreground location permission is granted
            if (isForegroundPermissionGranted())
              Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(requireContext()
                    , "Foreground Location permission is needed to show current location"
                    ,Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        isMapReady=false
    }

//    @SuppressLint("MissingPermission")
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if ((isForegroundPermissionGranted()
//                &&grantResults[0]==PackageManager.PERMISSION_GRANTED) && isMapReady
//        )
//        map.isMyLocationEnabled=true
//        else if(grantResults[0]==PackageManager.PERMISSION_DENIED)

//    }
}
private const val TAG="SelectLocationFragment"
