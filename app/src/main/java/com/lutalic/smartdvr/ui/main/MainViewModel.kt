package com.lutalic.smartdvr.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.lutalic.smartdvr.model.NeuroSkyRepository

class MainViewModel : ViewModel() {
    val neuroSkyRepository = NeuroSkyRepository()

    fun neuroSkyDisconnect() {
        neuroSkyRepository.disconnect()
    }

    fun neuroSkyConnect() {
        neuroSkyRepository.connect()
    }

    fun neuroSkyStart() {
        neuroSkyRepository.start()
    }

    val attention = neuroSkyRepository.attention.asLiveData()
    val meditation = neuroSkyRepository.meditation.asLiveData()
    val fatigue = neuroSkyRepository.fatigue.asLiveData()

}