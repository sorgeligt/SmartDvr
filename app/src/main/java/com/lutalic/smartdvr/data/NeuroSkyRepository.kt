package com.lutalic.smartdvr.data

import android.os.Handler
import com.github.pwittchen.neurosky.library.NeuroSky
import com.github.pwittchen.neurosky.library.listener.ExtendedDeviceMessageListener
import com.github.pwittchen.neurosky.library.message.enums.BrainWave
import com.github.pwittchen.neurosky.library.message.enums.Signal
import com.github.pwittchen.neurosky.library.message.enums.State
import com.lutalic.smartdvr.domain.BrainRepository
import com.lutalic.smartdvr.domain.StatisticsRepository
import com.lutalic.smartdvr.getCurrentDateWithoutLastSecond
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.Double.max
import java.lang.Double.min
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.random.Random


class NeuroSkyRepository(
    private val statisticsRepository: StatisticsRepository
) : BrainRepository(statisticsRepository) {


    override val attention: MutableStateFlow<String> = MutableStateFlow("Attention: 0")
    override val meditation: MutableStateFlow<String> = MutableStateFlow("Meditation: 0")
    override val fatigue: MutableStateFlow<String> = MutableStateFlow("Risk of fatigue: Unknown")

    private var lastAttention = 80
    private var lastMeditation = 20


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
                else -> {}
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
            } else if (valRisk < 10000000) {
                "Height"
            } else {
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
                statisticsRepository.addAttention(
                    getCurrentDateWithoutLastSecond(),
                    lastAttention.toDouble()
                )
                attention.value = getFormattedMessage("Attention: %d", signal)
            }
            Signal.MEDITATION -> {
                lastMeditation = signal.value
                statisticsRepository.addMeditation(
                    getCurrentDateWithoutLastSecond(),
                    lastMeditation.toDouble()
                )
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
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                var attentionn: Int = abs(lastAttention + (abs(Random.nextInt()) % 30 - 15)) % 100
                if(attentionn < 10){
                    attentionn = 80
                }
                val meditationn: Int = abs(lastAttention + (abs(Random.nextInt()) % 40 - 20)) % 100

                lastAttention = attentionn
                statisticsRepository.addAttention(
                    getCurrentDateWithoutLastSecond(),
                    lastAttention.toDouble()
                )
                attention.value = "Attention: $attentionn"

                lastMeditation = meditationn
                statisticsRepository.addMeditation(
                    getCurrentDateWithoutLastSecond(),
                    lastMeditation.toDouble()
                )
                meditation.value = "Meditation:$meditationn"

                if(Random.nextBoolean())
                    fatigue.value = "Risk of fatigue: Low"
                if(Random.nextBoolean())
                    fatigue.value = "Risk of fatigue: Medium"
                handler.postDelayed(this, 1000)

            }
        })
    }


    override fun disconnect() {
        neuroSky.disconnect()
    }

    override fun start() {
        neuroSky.startMonitoring()
    }

    override fun stop() {
        neuroSky.stopMonitoring()
    }

    override fun getAttentionStatistics(): HashMap<Long, Double> {
        return statisticsRepository.getAllAttentions()
    }

    override fun getMeditationStatistics(): Map<Long, Double> {
        return statisticsRepository.getAllMeditations()
    }
}