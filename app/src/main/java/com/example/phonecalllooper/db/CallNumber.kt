package com.example.phonecalllooper.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Numbers")
data class CallNumber(
    @ColumnInfo(name = "number")
    var number:String
    ){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="key")
    var key:Int=0
}