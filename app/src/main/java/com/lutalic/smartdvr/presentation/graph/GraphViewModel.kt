package com.lutalic.smartdvr.presentation.graph

import androidx.lifecycle.ViewModel
import com.jjoe64.graphview.series.DataPoint
import com.lutalic.smartdvr.domain.BrainRepository

class GraphViewModel(
    private val brainRepository: BrainRepository
) : ViewModel() {
    fun getAllAttentionData(): Array<DataPoint> {
        val statistics = brainRepository.getAttentionStatistics()
        val array = mutableListOf<DataPoint>()
        for (statistic in statistics) {
            array.add(DataPoint(statistic.key.toDouble(), statistic.value))
        }
        return array.toTypedArray()
    }

    fun getAllMeditationData(): Array<DataPoint> {
        val statistics = brainRepository.getMeditationStatistics()
        val array = mutableListOf<DataPoint>()
        for (statistic in statistics) {
            array.add(DataPoint(statistic.key.toDouble(), statistic.value))
        }
        return array.toTypedArray()
    }
}