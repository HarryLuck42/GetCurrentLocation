package com.corp.luqman.gettrackcoordinate.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coordinate_table" )
data class Coordinate(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id : Long = 0,
    @ColumnInfo(name = "latitude")
    var latitude: String? = "",
    @ColumnInfo(name = "longitude")
    var longitude: String? = ""
)