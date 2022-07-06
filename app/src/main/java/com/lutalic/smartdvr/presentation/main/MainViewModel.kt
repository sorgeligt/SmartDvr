package com.lutalic.smartdvr.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.lutalic.smartdvr.domain.BrainRepository

class MainViewModel(
    private val brainRepository: BrainRepository
) : ViewModel() {


    val attention = brainRepository.attention.asLiveData()
    val meditation = brainRepository.meditation.asLiveData()

    val fatigue = brainRepository.fatigue.asLiveData()

    fun disconnect() {
        brainRepository.disconnect()
    }

    fun connect() {
        brainRepository.connect()
    }

    fun start() {
        brainRepository.start()
    }

}