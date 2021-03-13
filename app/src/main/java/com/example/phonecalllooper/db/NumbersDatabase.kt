package com.example.phonecalllooper.db

import androidx.room.Database
import androidx.room.RoomDatabase
@Database(version = 1, entities = [CallNumber::class])
abstract class NumbersDatabase: RoomDatabase() {
    abstract val dao:NumbersDAO
}