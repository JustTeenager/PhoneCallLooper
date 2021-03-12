package com.example.phonecalllooper

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class PhoneStateChangedReceiver(val dropSec:Long, var numList: MutableList<String>) : BroadcastReceiver() {
    var prev_state = -2288

    var numIterator = 0
    lateinit var countDownTimer:CountDownTimer
    override fun onReceive(context: Context?, intent: Intent?) {

        val telephonyManager:TelephonyManager=context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millis: Long) {
                Log.d("tut_tick",millis.toString())
            }

            override fun onFinish() {
                disableRingingPhone(context)
            }
        }
        val phoneStateListener=object:PhoneStateListener(){
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                super.onCallStateChanged(state, phoneNumber)
                when(state){
                    TelephonyManager.CALL_STATE_RINGING ->
                    {
                        countDownTimer.cancel()

                        Log.d("tut","Ringing")
                        prev_state=state
                        countDownTimer.start()
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK ->
                    {
                        countDownTimer.cancel()

                        Log.d("tut","OFFHOOK")
                        prev_state=state
                        countDownTimer.start()
                    }
                    TelephonyManager.CALL_STATE_IDLE ->
                    {
                        countDownTimer.cancel()
                        if (prev_state==TelephonyManager.CALL_STATE_RINGING){
                            //Не приняли трубку
                            callToNextNum(context)
                        }
                        else if (prev_state==TelephonyManager.CALL_STATE_OFFHOOK){
                            //Приняли трубку
                            callToNextNum(context)
                        }
                        Log.d("tut","IDLE")

                    }
                }
            }
        }
        telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun callToNextNum(context: Context?) {
        numIterator++
        if (numIterator >= numList.size) numIterator = 0
        callToNum(context, numList[numIterator])
    }

    private fun callToNum(context: Context?, phoneNum: String) {
        Log.d("tut","звоним")

        val dial = "tel:$phoneNum"
        if (context != null) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse(dial))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            ContextCompat.startActivity(context, intent, null)
        } else Log.d("tut", "налл в контексте")
    }

    @SuppressLint("MissingPermission")
    fun disableRingingPhone(context: Context?):Boolean {
        Log.d("tut","disable")
        val telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            var m1: Method? = null
            try {
                m1 = telephonyManager.javaClass.getDeclaredMethod("getITelephony")
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }
            m1!!.isAccessible = true
            var iTelephony: Any? = null
            try {
                iTelephony = m1.invoke(telephonyManager)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
            var m3: Method? = null
            try {
                m3 = iTelephony!!.javaClass.getDeclaredMethod("endCall")
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }
            try {
                m3!!.invoke(iTelephony)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val telecomManager: TelecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.endCall()
        }
        return true
    }


}