package com.corp.luqman.gettrackcoordinate.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.corp.luqman.gettrackcoordinate.data.model.Coordinate

@Database(entities = [Coordinate::class], version = 1, exportSchema = false)
abstract class CoordinatesDatabase : RoomDatabase(){
    abstract fun coordinateDao(): CoordinateDao

    companion object {
        @Volatile
        private var INSTANCE: CoordinatesDatabase? = null

        fun getInstance(context: Context): CoordinatesDatabase {
            synchronized(this){
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        CoordinatesDatabase::class.java,
                        "coordinate_database"
                    ).fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }

                return instance
            }

        }
    }
}