package com.corp.luqman.gettrackcoordinate.ui.fragment

import androidx.lifecycle.LiveData
import com.corp.luqman.gettrackcoordinate.data.model.Coordinate

interface GetCoordinateInteractor {
    fun checkPermission(): Boolean
    fun requestPermission()
    fun locationisEnabled(): Boolean
    fun insertCoordinate(coordinate: Coordinate)
    fun clearAll()
    fun getAllCoordinate(): LiveData<MutableList<Coordinate>>
    fun getValueCoordinate(lat: String, lng: String): Coordinate
}