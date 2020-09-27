package com.corp.luqman.gettrackcoordinate.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.corp.luqman.gettrackcoordinate.data.model.Coordinate

@Dao
interface CoordinateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateLocation(coordinate: Coordinate)

    @Query("DELETE FROM coordinate_table")
    fun clearLocation()

    @Query("SELECT * FROM coordinate_table ORDER BY id ASC")
    fun getAllCoordinate(): LiveData<MutableList<Coordinate>>
}