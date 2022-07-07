package com.lutalic.smartdvr.data

import com.lutalic.smartdvr.domain.StatisticsRepository

class MedianStatisticsRepository : StatisticsRepository {
    private val attentionMap: HashMap<Long, Double> = hashMapOf()
    private val meditationMap: HashMap<Long, Double> = hashMapOf()

    override fun addAttention(key: Long, value: Double) {
        attentionMap[key] = ((attentionMap[key] ?: value) + value) / 2
    }

    override fun getAllAttentions() = attentionMap

    override fun addMeditation(key: Long, value: Double) {
        meditationMap[key] = ((meditationMap[key] ?: value) + value) / 2
    }

    override fun getAllMeditations() = meditationMap
}