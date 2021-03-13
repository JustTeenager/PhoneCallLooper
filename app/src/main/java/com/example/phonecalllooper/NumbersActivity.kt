package com.example.phonecalllooper

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.telecom.Call
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.phonecalllooper.databinding.ActivityNumbersBinding
import com.example.phonecalllooper.databinding.ItemNumberBinding
import com.example.phonecalllooper.db.DBSingleton
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

class NumbersActivity: AppCompatActivity() {
    private var numbers = mutableListOf<String>()
    private val bindingNumbers:ActivityNumbersBinding by lazy { DataBindingUtil.setContentView(this,R.layout.activity_numbers) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val flowable: Flowable<String> = DBSingleton.createDao(this)
                .getNumbers().subscribeOn(Schedulers.io()).flatMapIterable { it }.map { it.number }
        flowable.subscribe{
            numbers.add(it)
        }
        bindingNumbers.numbersRecyclerView.layoutManager = LinearLayoutManager(this)
        bindingNumbers.numbersRecyclerView.adapter = NumbersAdapter()
    }


    inner class NumbersAdapter:RecyclerView.Adapter<NumberHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberHolder {
            val binding:ItemNumberBinding = DataBindingUtil.inflate(LayoutInflater.from(this@NumbersActivity),R.layout.item_number,parent,false)
            return NumberHolder(binding)
        }

        override fun onBindViewHolder(holder: NumberHolder, position: Int) {
            holder.onBind(numbers[position])
        }

        override fun getItemCount()= numbers.size

    }

    inner class NumberHolder(val binding:ItemNumberBinding):RecyclerView.ViewHolder(binding.root){

        fun onBind(number: String){
            binding.textNumber.text = number
        }
    }

}