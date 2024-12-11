package com.example.weatherapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class HeavyTaskIndicator (application: Application) : AndroidViewModel(application) {

    private val _isCompleted = MutableLiveData<Boolean>()
    val isCompleted: LiveData<Boolean> get() = _isCompleted

    fun doHeavyTask(task: () -> Unit) = runBlocking{
        launch {
            task()
            _isCompleted.value = true
        }
    }

    fun setCompletedFalse(){
        _isCompleted.value = false
    }
}