package com.lutalic.smartdvr.model

import kotlinx.coroutines.flow.MutableStateFlow

interface BrainRepository {
    fun disconnect()

    val attention: MutableStateFlow<String>
    val meditation: MutableStateFlow<String>
    val heartRate: MutableStateFlow<String>
    fun connect()
    fun start()
    fun stop()
}