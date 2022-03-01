package com.berkeruysal.travelbookotlin.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.berkeruysal.travelbookotlin.model.Place

@Database(entities = [Place::class], version = 1)
    abstract class PlaceDatabase:RoomDatabase() //Room database'den kalıtım alıyor.
    {
        abstract fun placeDao():PlaceDao
    }
