package com.example.phonecalllooper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.content.ContextCompat



class SMSReceiver(private val callback: Callback) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val pudsBundle:Bundle? = intent?.extras
        val pdus:Array<Any> = pudsBundle?.get("pdus") as Array<Any>
        for (element in pdus) {
            val message:SmsMessage =SmsMessage.createFromPdu(element as ByteArray)

            callback.setupCallLoop(message.originatingAddress,message.messageBody)


            //телефонноый номер
            Log.i("tut", "Bingo! Received SMS from: " + message.originatingAddress);

            //сообщение
            Log.i("tut", "msg_body" + message.messageBody)
        }

        abortBroadcast()
    }

    interface Callback{
        fun setupCallLoop(originatingAddress: String?, messageBody: String)
    }
}