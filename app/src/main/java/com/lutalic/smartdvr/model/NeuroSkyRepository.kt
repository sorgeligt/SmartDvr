package com.lutalic.smartdvr.model

import com.github.pwittchen.neurosky.library.NeuroSky
import com.github.pwittchen.neurosky.library.listener.ExtendedDeviceMessageListener
import com.github.pwittchen.neurosky.library.message.enums.BrainWave
import com.github.pwittchen.neurosky.library.message.enums.Signal
import com.github.pwittchen.neurosky.library.message.enums.State
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class NeuroSkyRepository : BrainRepository {


    override val attention: MutableStateFlow<String> = MutableStateFlow("Attention: 0")
    override val meditation: MutableStateFlow<String> = MutableStateFlow("Meditation: 0")
    override val heartRate: MutableStateFlow<String> = MutableStateFlow("Heart rate: 0")


    private val neuroSky = NeuroSky(object : ExtendedDeviceMessageListener() {
        override fun onStateChange(state: State) {
            handleStateChange(state)
        }

        override fun onSignalChange(signal: Signal) {
            handleSignalChange(signal)
        }

        override fun onBrainWavesChange(brainWaves: Set<BrainWave>) {

        }
    })

    private fun handleStateChange(state: State) {
        if (state == State.CONNECTED) {
            neuroSky.startMonitoring()
        }
        if (state == State.DISCONNECTED) {
            neuroSky.stopMonitoring()
        }
    }

    private fun handleSignalChange(signal: Signal) {
        when (signal) {
            Signal.ATTENTION -> attention.value = getFormattedMessage("attention: %d", signal)
            Signal.MEDITATION -> meditation.value  = getFormattedMessage("meditation: %d", signal)
            Signal.HEART_RATE -> heartRate.value  = getFormattedMessage("meditation: %d", signal)
            else -> {}
        }

    }

    private fun getFormattedMessage(
        messageFormat: String,
        signal: Signal
    ): String {
        return String.format(Locale.getDefault(), messageFormat, signal.value)
    }

    override fun connect(){
        neuroSky.connect()
    }


    override fun start() {
        neuroSky.startMonitoring()
    }

    override fun stop() {
        neuroSky.stopMonitoring()
    }

    override fun disconnect() {
        neuroSky.disconnect()
    }
}