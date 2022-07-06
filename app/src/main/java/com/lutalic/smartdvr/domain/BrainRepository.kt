package com.lutalic.smartdvr.domain

import kotlinx.coroutines.flow.MutableStateFlow

abstract class BrainRepository(private val statisticsRepository: StatisticsRepository) {
    abstract val attention: MutableStateFlow<String>
    abstract val meditation: MutableStateFlow<String>
    abstract val fatigue: MutableStateFlow<String>

    abstract fun disconnect()
    abstract fun connect()
    abstract fun start()
    abstract fun stop()

    abstract fun getAttentionStatistics(): Map<Long, Double>
    abstract fun getMeditationStatistics(): Map<Long, Double>
}