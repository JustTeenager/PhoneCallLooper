package com.example.phonecalllooper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class MySharedPreferences {

    companion object{
        private const val SHARED_KEY = " shared_key"
        private const val MAIN_NUMBER_KEY="main_number_key"
        fun readData(context: Context):String{
            return context.getSharedPreferences(SHARED_KEY,Context.MODE_PRIVATE).getString(
                MAIN_NUMBER_KEY,"data").toString()
        }

        @SuppressLint("CommitPrefEdits")
        fun writeData(context: Context, data:String){
            val sharedPreferences:SharedPreferences.Editor = context.getSharedPreferences(SHARED_KEY,Context.MODE_PRIVATE).edit()
            sharedPreferences.putString(MAIN_NUMBER_KEY, data).apply()
        }
    }
}