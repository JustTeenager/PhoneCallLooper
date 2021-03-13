package com.example.phonecalllooper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.room.Room
import com.example.phonecalllooper.databinding.ActivityMainBinding
import com.example.phonecalllooper.db.CallNumber
import com.example.phonecalllooper.db.DBSingleton
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(),SMSReceiver.Callback {
    private lateinit var phoneStateChangedReceiver:PhoneStateChangedReceiver
    private lateinit var smsReceiver:SMSReceiver
    private var numsList:MutableList<String> = mutableListOf()
    //private lateinit var db:NumbersDatabase

    private val action = "android.provider.Telephony.SMS_RECEIVED"
    private val filterRingtones=IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
    private val filterSMS=IntentFilter(action)

    private val binding:ActivityMainBinding by lazy{DataBindingUtil.setContentView(this, R.layout.activity_main)}

    private val disposables:CompositeDisposable=CompositeDisposable()




    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.CALL_PHONE,Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
       // db = Room.databaseBuilder(this, NumbersDatabase::class.java,"database" )
         //   .build()
        if (MySharedPreferences.readManagerNumber(this@MainActivity).isEmpty()){
            Toast.makeText(this,getString(R.string.enter_number_setter), Toast.LENGTH_SHORT).show()
        }
        else{
            //val flowable:Flowable<String> = db.dao.getNumbers().subscribeOn(Schedulers.io()).flatMapIterable { it }.map { it.number }
            val flowable:Flowable<String> = DBSingleton.createDao(this)
                    .getNumbers().subscribeOn(Schedulers.io()).flatMapIterable { it }.map { it.number }
            flowable.subscribe{
                numsList.add(it)
            }
        }



        setupFilters(filterSMS, filterRingtones)
        setOnClickAllButton(filterRingtones)

    }

    private fun setOnClickAllButton(filterRingtones: IntentFilter) {
        binding.startBtn.setOnClickListener {
            binding.dropSec.isEnabled = false
            binding.startBtn.isEnabled = false
            binding.changeNum.isEnabled = false
            binding.checkNumbers.isEnabled=false
            binding.stopBtn.isEnabled = true

           // registerReceiver(phoneStateChangedReceiver, filterRingtones)
            if (binding.dropSec.text.isEmpty())
            createLoopCalls( MySharedPreferences.readPeriodDisable(this))
            else {
                createLoopCalls(binding.dropSec.text.toString().toLong())
                MySharedPreferences.writePeriodDisable(this,binding.dropSec.text.toString().toLong())
            }
           // callToNum(this, numsList[0])
        }

        binding.stopBtn.setOnClickListener {
            binding.dropSec.isEnabled = true
            binding.startBtn.isEnabled = true
            binding.changeNum.isEnabled = true
            binding.checkNumbers.isEnabled=true
            binding.stopBtn.isEnabled = false
            disposables.dispose()
           // unregisterReceiver(phoneStateChangedReceiver)
        }
        binding.changeNum.setOnClickListener {
            val dialog = ChangeMainNumDialog()
            dialog.isCancelable = false
            dialog.show(supportFragmentManager, null)
        }

        binding.checkNumbers.setOnClickListener {
            val intent = Intent(this@MainActivity,NumbersActivity::class.java)
            Log.d("tut","intent")
            startActivity(intent)
        }
    }

    private fun setupFilters(filterSMS: IntentFilter, filterRingtones: IntentFilter) {
        filterSMS.priority = 100
        filterRingtones.priority = 100
        smsReceiver = SMSReceiver(this)
        phoneStateChangedReceiver = PhoneStateChangedReceiver(
                dropSec = (if (binding.dropSec.text.toString().isNotEmpty()) binding.dropSec.text.toString().toLong() else 10), numsList)

        registerReceiver(smsReceiver, filterSMS)
        //registerReceiver(phoneStateChangedReceiver, filterRingtones)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        if (originatingAddress!=MySharedPreferences.readManagerNumber(this)) return
        if (disposables.isDisposed) disposables.dispose()
        //disableRingingPhone()
        //unregisterReceiver(phoneStateChangedReceiver)
        //db.dao.deleteNumbers()
        numsList.clear()

        val observable:Observable<String> =
            Observable.just(messageBody).subscribeOn(Schedulers.io())
                .flatMap { s->Observable.fromIterable(s.split("\n")) }

        val observer:DisposableObserver<String> = object:DisposableObserver<String>(){
            private var flagFirstString = true
            override fun onNext(t: String) {
                if (flagFirstString){
                    flagFirstString=false
                    DBSingleton.createDao(this@MainActivity).deleteNumbers()
                    MySharedPreferences.writePeriodDisable(this@MainActivity, t.toLong())
                }
                else{
                DBSingleton.createDao(this@MainActivity)
                        .insertNumber(CallNumber(number = t))
                numsList.add(t)
                }

            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {
                //TODO: раскомментить звонок
                //registerReceiver(phoneStateChangedReceiver, filterRingtones)
                //callToNum(this,numsList[0])
                createLoopCalls(MySharedPreferences.readPeriodDisable(this@MainActivity))
            }
        }
        observable.subscribe(observer)
    }

    @SuppressLint("CheckResult")
    private fun createLoopCalls(period:Long){
        val observerForLoopCalls = object: DisposableObserver<Long>() {
            var count = 0
            override fun onNext(t: Long) {
                Log.d("tut_count",count.toString())
                Log.d("tut_call",t.toString())
                if (!isDisposed) {
                    callToNum(numsList[count])
                    if (count < numsList.size - 1)
                        count++
                    else count = 0
                }
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

            }
        }
        val observerForCallDisable= object: DisposableObserver<Long>(){
            override fun onNext(t: Long) {
                Log.d("tut_delay",t.toString())
                disableRingingPhone()
            }

            override fun onError(e: Throwable) {
            }

            override fun onComplete() {
            }

        }
        //TODO: в период  и делай вставить данные из sharedPreferences
        val observable:Observable<Long> = Observable.interval(2,period+8,TimeUnit.SECONDS).subscribeOn(Schedulers.io())
        observable.subscribe(observerForLoopCalls)
        observable.delay(period,TimeUnit.SECONDS).subscribe(observerForCallDisable)
        disposables.addAll(observerForLoopCalls,observerForCallDisable)
    }

    private fun callToNum(phoneNum: String) {
        Log.d("tut", "звоним")

        val dial = "tel:$phoneNum"
        val intent = Intent(Intent.ACTION_CALL, Uri.parse(dial))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ContextCompat.startActivity(this, intent, null)
    }

    @SuppressLint("MissingPermission")
    fun disableRingingPhone(){
        Log.d("tut","disable")
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
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
            val telecomManager: TelecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            telecomManager.endCall()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
        //unregisterReceiver(phoneStateChangedReceiver)
    }
}