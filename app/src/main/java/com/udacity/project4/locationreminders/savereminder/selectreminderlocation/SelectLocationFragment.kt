package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*
private const val  REQUEST_LOCATION_PERMISSION=1
val androidQ=(Build.VERSION.SDK_INT == Build.VERSION_CODES.Q)
val androidR_Plus=(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
class SelectLocationFragment : BaseFragment(),OnMapReadyCallback {
    //Use Koin to get the view model of the SaveReminder

    private var isMapReady=false
    override val _viewModel: SaveReminderViewModel by inject()
    private var isMarkerAdded=false
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var confirmDialog:AlertDialog
    private lateinit var map:GoogleMap
    private lateinit var selectedPOI: PointOfInterest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        val mapFragment= childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: zoom to the user location after taking his permission

        binding.saveBtn.setOnClickListener {
            if (isMarkerAdded)
                onLocationSelected()
            else {
                Toast.makeText(requireContext(), "Please select location", Toast.LENGTH_LONG).show()
                Log.e("tag","No Location")
            }
        }


        return binding.root
    }


    private fun onLocationSelected() {

        val confirmDialogBuilder = AlertDialog.Builder(context).apply {
            setTitle(R.string.app_name)
            setMessage("Save selected location ?")
            setIcon(R.drawable.icauncherforeground)
            setPositiveButton("Yes") { dialog, id ->
                dialog.dismiss()
                Snackbar.make(requireView(),"Location Saved",Snackbar.LENGTH_SHORT).show()
                _viewModel.latitude.value=selectedPOI.latLng.latitude
                _viewModel.longitude.value=selectedPOI.latLng.longitude
                _viewModel.reminderSelectedLocationStr.value=selectedPOI.name
                _viewModel.selectedPOI.value=selectedPOI
                _viewModel.navigationCommand.value =
                    NavigationCommand.Back
            }
            setNegativeButton(
                "No"
            ) { dialog, id ->
                dialog.dismiss()
            }
        }

        confirmDialog = confirmDialogBuilder.create()
        confirmDialog.show()

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType=GoogleMap.MAP_TYPE_NORMAL
            enableMyLocation()
            true
        }
        R.id.hybrid_map -> {
            map.mapType=GoogleMap.MAP_TYPE_HYBRID
            enableMyLocation()
            true
        }
        R.id.satellite_map -> {
            map.mapType=GoogleMap.MAP_TYPE_SATELLITE
            enableMyLocation()
            true
        }
        R.id.terrain_map -> {
            map.mapType=GoogleMap.MAP_TYPE_TERRAIN
            enableMyLocation()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        isMapReady=true
        map = googleMap
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),R.raw.map_style))
       setMapLongClick(map)
        setPoiClick(map)
       enableMyLocation()
    }
    //Check if Location Permissions are granted

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    private fun isPermissionGranted(): Boolean{
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()

        if (isMapReady&&ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            map.isMyLocationEnabled=true

     if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_BACKGROUND_LOCATION)==PackageManager.PERMISSION_GRANTED)
         Snackbar.make(requireView(),"Permission Granted",Snackbar.LENGTH_SHORT).show()
    }

    


    @SuppressLint("InlinedApi")
    private fun enableMyLocation(){
        if(isPermissionGranted()){
            if (androidQ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
            map.isMyLocationEnabled=true
        }
        else{
            // Permission denied.
            Snackbar.make(
                requireView(),
                R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    // Displays App settings screen.
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
            val req=   arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION)

                ActivityCompat.requestPermissions(
                    requireActivity(),
                    req,
                    REQUEST_LOCATION_PERMISSION
                )


            if (androidR_Plus) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    ,REQUEST_LOCATION_PERMISSION)
            }

        }
        if (androidR_Plus) {


            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(requireContext(),"App needs 'All the time' location permission To set Geofence",
                    Toast.LENGTH_LONG).show()
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
                Snackbar.make(
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
                    }.show()
            }
        }

    }

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
            if (isMarkerAdded) {
                map.clear()
            }
            val marker=map.addMarker(
                MarkerOptions().position(it)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )
            map.addCircle(CircleOptions().center(it).radius(120.0))
            if (marker!=null)
            selectedPOI= PointOfInterest(marker.position,marker.title.toString(),marker.title.toString())
            isMarkerAdded=true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isMapReady=false
    }


}
