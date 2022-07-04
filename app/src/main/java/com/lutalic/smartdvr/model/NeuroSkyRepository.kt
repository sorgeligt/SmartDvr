package com.lutalic.smartdvr.model

import com.github.pwittchen.neurosky.library.NeuroSky
import com.github.pwittchen.neurosky.library.listener.ExtendedDeviceMessageListener
import com.github.pwittchen.neurosky.library.message.enums.BrainWave
import com.github.pwittchen.neurosky.library.message.enums.Signal
import com.github.pwittchen.neurosky.library.message.enums.State
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.Double.max
import java.lang.Double.min
import java.util.*

class NeuroSkyRepository : BrainRepository {


    override val attention: MutableStateFlow<String> = MutableStateFlow("Attention: 0")
    override val meditation: MutableStateFlow<String> = MutableStateFlow("Meditation: 0")
    override val fatigue: MutableStateFlow<String> = MutableStateFlow("Risk of fatigue: Unknown")

    var lastAttention = 0
    var lastMeditation = 0


    private val neuroSky = NeuroSky(object : ExtendedDeviceMessageListener() {
        override fun onStateChange(state: State) {
            handleStateChange(state)
        }

        override fun onSignalChange(signal: Signal) {
            handleSignalChange(signal)
        }

        override fun onBrainWavesChange(brainWaves: Set<BrainWave>) {
            handleBrainWavesChange(brainWaves)
        }
    })

    private fun handleBrainWavesChange(brainWaves: Set<BrainWave>) {
        if (lastAttention + lastMeditation == 0) {
            fatigue.value = "Risk of fatigue: Unknown"
            return
        }
        var beta: Double = (-1).toDouble()
        var alpha: Double = (-1).toDouble()
        var theta: Double = (-1).toDouble()

        for (brainWave in brainWaves) {
            when (brainWave) {
                BrainWave.LOW_BETA -> beta = brainWave.value.toDouble()
                BrainWave.THETA -> theta = brainWave.value.toDouble()
                BrainWave.LOW_ALPHA -> alpha = brainWave.value.toDouble()
            }
        }
        val risk: String
        if (beta < 0 || alpha < 0 || theta < 0) {
            return
        } else {
            val first: Double = beta / alpha
            val second = (theta + alpha) / beta
            val valRisk: Double = max(first, second) / min(first, second)
            risk = if (valRisk < 200) {
                "Low"
            } else if (valRisk < 15000) {
                "Medium"
            } else if (valRisk < 1000000) {
                if (Random().nextBoolean()) {
                    return
                }
                "Height"
            } else {
                if (Random().nextBoolean()) {
                    return
                }
                if (Random().nextBoolean()) {
                    return
                }
                "Very High"
            }
        }
        fatigue.value = "Risk of fatigue: $risk"
    }

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
            Signal.ATTENTION -> {
                lastAttention = signal.value
                attention.value = getFormattedMessage("Attention: %d", signal)
            }
            Signal.MEDITATION -> {
                lastMeditation = signal.value
                meditation.value = getFormattedMessage("Meditation: %d", signal)
            }
            else -> {}
        }

    }

    private fun getFormattedMessage(
        messageFormat: String,
        signal: Signal
    ): String {
        return String.format(Locale.getDefault(), messageFormat, signal.value)
    }

    override fun connect() {
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