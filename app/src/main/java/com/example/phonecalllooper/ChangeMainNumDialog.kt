package com.example.phonecalllooper

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.phonecalllooper.databinding.DialogChangeMainNumBinding

class ChangeMainNumDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding:DialogChangeMainNumBinding=DataBindingUtil.inflate(LayoutInflater.from(context),R.layout.dialog_change_main_num,null,false)
        binding.changeNumEditText.setText(context?.let { MySharedPreferences.readManagerNumber(it) })
        val builder:AlertDialog.Builder=AlertDialog.Builder(context)
                return builder.setPositiveButton(R.string.OK,object: DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                           Log.d("tut_dialog", binding.changeNumEditText.text.toString())
                           context?.let {
                               MySharedPreferences.writeManagerNumber(
                                   it,
                                   binding.changeNumEditText.text.toString()
                               )
                           }
                        dismiss()
                    }
                }).setTitle(R.string.settings_num_text).setView(binding.root).create()
    }
}