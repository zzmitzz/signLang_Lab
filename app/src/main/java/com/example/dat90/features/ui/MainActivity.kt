package com.example.dat90.features.ui

import android.content.Intent
import android.os.Bundle
import com.example.dat90.core.platform.BaseActivity
import com.example.dat90.databinding.ActivityMainBinding
import androidx.activity.viewModels

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val viewModel: MainActivityVM by viewModels<MainActivityVM>()
    override fun initViewBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
    }

    override fun requestPermissions() {
    }

    override fun doThingsOnCreate() {
        binding.button.setOnClickListener{
            startActivity(Intent(this, CameraDetectionActivity::class.java))
        }
    }

}