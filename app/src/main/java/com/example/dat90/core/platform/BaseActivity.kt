package com.example.dat90.core.platform

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VH: ViewBinding> : AppCompatActivity() {
    lateinit var binding: VH
    abstract fun initViewBinding()
    abstract fun requestPermissions()
    abstract fun doThingsOnCreate()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewBinding()
        setContentView(binding.root)
        requestPermissions()
        doThingsOnCreate()
    }



}