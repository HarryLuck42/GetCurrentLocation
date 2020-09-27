package com.corp.luqman.gettrackcoordinate.ui.fragment

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.corp.luqman.gettrackcoordinate.R
import com.corp.luqman.gettrackcoordinate.data.model.Coordinate
import com.corp.luqman.gettrackcoordinate.ui.adapter.LocationAdapter
import com.corp.luqman.gettrackcoordinate.utils.Consts
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.get_coordinate_fragment.view.*

class GetCoordinateFragment : Fragment() {



    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var viewRoot : View
    private var handler = Handler()
    private lateinit var runnable: Runnable
    private var listLocation: MutableList<Coordinate> = mutableListOf()
    private lateinit var adapter : LocationAdapter
    private lateinit var presenter: GetCoordinatePresenter
    private var method = Consts.METHOD_CALLBACK


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("State LifeCycle","onCreateView Called")
        setHasOptionsMenu(true)
        viewRoot = inflater.inflate(R.layout.get_coordinate_fragment, container, false)
        settingAdapterLocation()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(viewRoot.context)
        presenter = GetCoordinatePresenter(this)
        presenter.clearAll()
        viewRoot.btn_start.isEnabled = true
        viewRoot.btn_stop.isEnabled = false
        presenter.getAllCoordinate().observe(viewLifecycleOwner, Observer {
            listLocation.clear()
            listLocation.addAll(it)
            adapter.notifyDataSetChanged()
        })
        viewRoot.btn_start.setOnClickListener {
            when(method){
                Consts.METHOD_RUNNABLE->{
                    startGetLocation()
                }
                Consts.METHOD_CALLBACK->{
                    startCoordinateWithPermission()
                }
            }



        }

        viewRoot.btn_stop.setOnClickListener {
            when(method){
                Consts.METHOD_RUNNABLE->{
                    stopGetLocation()
                }
                Consts.METHOD_CALLBACK->{
                    stopCoordinateService()
                }
            }
        }

        return viewRoot
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_bar_app, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun resetState(){
        presenter.clearAll()
        viewRoot.btn_start.isEnabled = true
        viewRoot.btn_stop.isEnabled = false
        when(method){
            Consts.METHOD_RUNNABLE->{
                stopGetLocation()
            }
            Consts.METHOD_CALLBACK->{
                stopCoordinateService()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.reset_menu->{
                resetState()
            }
            R.id.method_callback ->{
                resetState()
                method = Consts.METHOD_CALLBACK
                Toast.makeText(context, context!!.getString(R.string.method_callback), Toast.LENGTH_SHORT)
                    .show()
            }
            R.id.method_runnable ->{
                resetState()
                method = Consts.METHOD_RUNNABLE
                Toast.makeText(context, context!!.getString(R.string.method_runnable), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun stopGetLocation() {
        viewRoot.btn_start.isEnabled = true
        viewRoot.btn_stop.isEnabled = false
        Toast.makeText(context, context!!.getString(R.string.stop), Toast.LENGTH_SHORT).show()
        handler.removeCallbacks(runnable)
    }

    private fun startGetLocation() {
        runnable = Runnable {
            getLastLocation()
            handler.postDelayed(runnable, 60000)
        }

        handler.postDelayed(runnable, 1000)
    }

    private fun settingAdapterLocation(){
        val layoutManager = LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false)
        viewRoot.rv_location.layoutManager = layoutManager
        viewRoot.rv_location.setHasFixedSize(true)
        adapter = LocationAdapter(listLocation)
        adapter.notifyDataSetChanged()
        viewRoot.rv_location.adapter = adapter
    }



    private fun buildAlertMessageNoGPS(){
        val dialog = AlertDialog.Builder(viewRoot.context)
        dialog.setTitle(context!!.getString(R.string.warning))
            .setMessage(context!!.getString(R.string.message_gps_is_disable))
            .setCancelable(false)
            .setPositiveButton("OK"){dialogis, which ->
                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }.create()
            .show()
    }

    private fun getLastLocation(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(presenter.checkPermission()){
                if(presenter.locationisEnabled()){
                    getLocation()
                }else{
                    buildAlertMessageNoGPS()
                }
            }else{
                presenter.requestPermission()
            }
        }else{
            getLocation()
        }

    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            val location = task.result
            if (location != null) {
                Toast.makeText(context, context!!.getString(R.string.start), Toast.LENGTH_SHORT).show()
                viewRoot.btn_stop.isEnabled = true
                viewRoot.btn_start.isEnabled = false
                val lat = location.latitude.toString()
                val lng = location.longitude.toString()
                val coordinate = presenter.getValueCoordinate(lat, lng)
                Toast.makeText(viewRoot.context, context!!.getString(R.string.update), Toast.LENGTH_SHORT).show()
                presenter.insertCoordinate(coordinate)
                Toast.makeText(context, context!!.getString(R.string.update), Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(context, context!!.getString(R.string.error), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == Consts.PERMISSION_LOCATION){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(presenter.locationisEnabled()){
                    getLastLocation()
                }else{
                    buildAlertMessageNoGPS()
                }
            }
        }
    }


    private fun startCoordinateWithPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(presenter.checkPermission()){
                if(presenter.locationisEnabled()){
                    startCoordinateService()
                }else{
                    buildAlertMessageNoGPS()
                }
            }else{
                presenter.requestPermission()
            }
        }else{
            getLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startCoordinateService(){


        val locationResult = LocationRequest()
        locationResult.setInterval(60000)
        locationResult.setFastestInterval(55000)
        locationResult.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        LocationServices.getFusedLocationProviderClient(viewRoot.context)
            .requestLocationUpdates(locationResult, locationCallback, Looper.getMainLooper())

    }

    private fun stopCoordinateService(){
        viewRoot.btn_start.isEnabled = true
        viewRoot.btn_stop.isEnabled = false
        LocationServices.getFusedLocationProviderClient(viewRoot.context)
            .removeLocationUpdates(locationCallback)
    }

    private val locationCallback : LocationCallback? = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            if(locationResult != null && locationResult.lastLocation != null){
                viewRoot.btn_start.isEnabled = false
                viewRoot.btn_stop.isEnabled = true
                val lat = locationResult.lastLocation.latitude
                val lng  = locationResult.lastLocation.longitude
                val coordinate = presenter.getValueCoordinate(lat.toString(), lng.toString())
                Toast.makeText(viewRoot.context, context!!.getString(R.string.update), Toast.LENGTH_SHORT).show()
                presenter.insertCoordinate(coordinate)
                adapter.notifyDataSetChanged()
            }
        }
    }


}