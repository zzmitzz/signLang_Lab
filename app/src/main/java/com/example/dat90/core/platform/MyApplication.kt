package com.example.dat90.core.platform

import android.app.Application

class MyApplication: Application() {
    companion object{
        private lateinit var instance: MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}