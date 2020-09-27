package com.corp.luqman.gettrackcoordinate.ui.fragment

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.lifecycle.LiveData
import com.corp.luqman.gettrackcoordinate.data.database.CoordinateDao
import com.corp.luqman.gettrackcoordinate.data.database.CoordinatesDatabase
import com.corp.luqman.gettrackcoordinate.data.model.Coordinate
import com.corp.luqman.gettrackcoordinate.utils.Consts

class GetCoordinatePresenter(private val v: GetCoordinateFragment) : GetCoordinateInteractor{
    val application = requireNotNull(v.activity).application
    private val coordinateDao: CoordinateDao = CoordinatesDatabase.getInstance(application).coordinateDao()

    private val allCoordinates: LiveData<MutableList<Coordinate>>

    init {
        allCoordinates = coordinateDao.getAllCoordinate()
    }
    override fun checkPermission() : Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (v.context!!.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                v.context!!.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            } else {
                return false
            }
        }else{
            return true
        }
    }

    override fun requestPermission(){
        requestPermissions(v.requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), Consts.PERMISSION_LOCATION)
    }

    override fun locationisEnabled() : Boolean{
        var locationManager = v.context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    override fun insertCoordinate(coordinate: Coordinate) {
        InsertAsyncTask(coordinateDao).execute(coordinate)
    }

    override fun clearAll() {
        val coordinateList = allCoordinates.value?.toTypedArray()
        if(coordinateList != null){
            DeleteAsyncTask(coordinateDao).execute()
        }

    }

    override fun getAllCoordinate() = allCoordinates

    private class InsertAsyncTask internal constructor(private val dao: CoordinateDao) : AsyncTask<Coordinate, Void, Void>(){
        override fun doInBackground(vararg params: Coordinate?): Void? {
            dao.updateLocation(params[0]!!)
            return null
        }

    }

    private class DeleteAsyncTask internal constructor(private val dao: CoordinateDao) : AsyncTask<Coordinate, Void, Void>(){
        override fun doInBackground(vararg params: Coordinate?): Void? {
            dao.clearLocation()
            return null
        }

    }

    override fun getValueCoordinate(lat: String, lng: String): Coordinate{
        val coordinate =
            Coordinate()
        coordinate.latitude = lat
        coordinate.longitude = lng
        return coordinate
    }

}