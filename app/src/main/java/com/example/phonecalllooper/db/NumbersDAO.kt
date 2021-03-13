package com.example.phonecalllooper.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface NumbersDAO {

    @Query("SELECT * FROM Numbers")
    fun getNumbers():Flowable<MutableList<CallNumber>>

    @Insert
    fun insertNumber(callNumber: CallNumber)

    @Query("DELETE FROM Numbers")
    fun deleteNumbers()
}