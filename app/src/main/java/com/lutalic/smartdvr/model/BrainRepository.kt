package com.lutalic.smartdvr.model

import kotlinx.coroutines.flow.MutableStateFlow

interface BrainRepository {
    val attention: MutableStateFlow<String>
    val meditation: MutableStateFlow<String>
    val fatigue: MutableStateFlow<String>

    fun disconnect()
    fun connect()
    fun start()
    fun stop()
}