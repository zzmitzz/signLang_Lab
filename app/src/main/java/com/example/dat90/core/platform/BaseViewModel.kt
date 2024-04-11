package com.example.dat90.core.platform

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

abstract class BaseViewModel: ViewModel() {
    val loadingState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun loadingComplete(){
        loadingState.value = true
    }
    fun loadingStart(){
        loadingState.value = false
    }

}