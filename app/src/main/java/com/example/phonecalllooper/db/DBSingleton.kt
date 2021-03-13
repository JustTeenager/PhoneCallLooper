package com.example.phonecalllooper.db

import android.content.Context
import android.util.Log
import androidx.room.Room

object DBSingleton {
    private var db:NumbersDatabase? = null
    fun createDao(context: Context):NumbersDAO{
        if (db == null){
            db = Room.databaseBuilder(context,NumbersDatabase::class.java,"database").build()
        }
        return db!!.dao
    }
}