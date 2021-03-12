package com.example.phonecalllooper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.phonecalllooper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),SMSReceiver.Callback {
    private lateinit var phoneStateChangedReceiver:PhoneStateChangedReceiver
    private lateinit var smsReceiver:SMSReceiver
    private var numsList:ArrayList<String> = ArrayList()

    val action = "android.provider.Telephony.SMS_RECEIVED"

    private val binding:ActivityMainBinding by lazy{DataBindingUtil.setContentView(this, R.layout.activity_main)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.CALL_PHONE,Manifest.permission.RECEIVE_SMS), 1)

        numsList.add("+79140440147")

        val filterRingtones=IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        val filterSMS=IntentFilter(action)

        setupFilters(filterSMS, filterRingtones)



        binding.startBtn.setOnClickListener {
            binding.dropSec.isEnabled=false
            binding.startBtn.isEnabled=false
            binding.changeNum.isEnabled=false
            binding.stopBtn.isEnabled=true
            registerReceiver(phoneStateChangedReceiver, filterRingtones)
            callToNum(this,numsList[0])
        }
        binding.stopBtn.setOnClickListener {
            binding.dropSec.isEnabled=true
            binding.startBtn.isEnabled=true
            binding.changeNum.isEnabled=true
            binding.stopBtn.isEnabled=false
            unregisterReceiver(phoneStateChangedReceiver)
        }
        binding.changeNum.setOnClickListener {
            val dialog = ChangeMainNumDialog()
            dialog.isCancelable=false
            dialog.show(supportFragmentManager,null)
        }

    }

    private fun setupFilters(filterSMS: IntentFilter, filterRingtones: IntentFilter) {
        filterSMS.priority = 100
        filterRingtones.priority = 100
        smsReceiver = SMSReceiver(this)
        phoneStateChangedReceiver = PhoneStateChangedReceiver(
                dropSec = (if (binding.dropSec.text.toString().isNotEmpty()) binding.dropSec.text.toString().toLong() else 10), numsList)

        registerReceiver(smsReceiver, filterSMS)
        registerReceiver(phoneStateChangedReceiver, filterRingtones)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("tut", "phone_calls_permissions")
            requestPermission(Manifest.permission.ANSWER_PHONE_CALLS, 4)
        }
    }

    private fun requestPermission(permission: String, num: Int){
        if (ActivityCompat.checkSelfPermission(this@MainActivity, permission)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(permission),
                    num
            )
        }
    }

    override fun setupCallLoop(originatingAddress: String?, messageBody: String) {

        unregisterReceiver(phoneStateChangedReceiver)

        //TODO Вот здесь шлепай эрикс + сравнение с главным номером
        //registerReceiver(новый ресивер)

        callToNum(this,numsList[0])
    }

    private fun callToNum(context: Context?, phoneNum: String) {
        Log.d("tut", "звоним")

        val dial = "tel:$phoneNum"
        if (context != null) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse(dial))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            ContextCompat.startActivity(context, intent, null)
        } else Log.d("tut", "налл в контексте")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(phoneStateChangedReceiver)
    }
}