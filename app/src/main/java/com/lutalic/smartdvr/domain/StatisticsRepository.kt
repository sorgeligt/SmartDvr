package com.lutalic.smartdvr.domain

interface StatisticsRepository {

    fun addAttention(key: Long, value: Double)

    fun getAllAttentions(): HashMap<Long, Double>

    fun addMeditation(key: Long, value: Double)

    fun getAllMeditations(): HashMap<Long, Double>

}