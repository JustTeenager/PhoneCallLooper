package com.example.phonecalllooper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class MySharedPreferences {

    companion object{
        private const val SHARED_KEY = " shared_key"
        private const val MAIN_NUMBER_KEY="main_number_key"
        private const val PERIOD_DISABLE_KEY = "period_disable_key"
        fun readManagerNumber(context: Context):String{
            return context.getSharedPreferences(SHARED_KEY,Context.MODE_PRIVATE).getString(
                MAIN_NUMBER_KEY,"").toString()
        }

        @SuppressLint("CommitPrefEdits")
        fun writeManagerNumber(context: Context, data:String){
            val sharedPreferences:SharedPreferences.Editor = context.getSharedPreferences(SHARED_KEY,Context.MODE_PRIVATE).edit()
            sharedPreferences.putString(MAIN_NUMBER_KEY, data).apply()
        }

        fun readPeriodDisable(context: Context):Long{
            return context.getSharedPreferences(SHARED_KEY, Context.MODE_PRIVATE).getLong(
                    PERIOD_DISABLE_KEY,0
            )
        }

        fun writePeriodDisable(context: Context, period:Long){
            val sharedPreferences:SharedPreferences.Editor = context.getSharedPreferences(SHARED_KEY,Context.MODE_PRIVATE).edit()
            sharedPreferences.putLong(PERIOD_DISABLE_KEY, period).apply()
        }
    }
}