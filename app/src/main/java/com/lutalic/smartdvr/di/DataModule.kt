package com.lutalic.smartdvr.di

import com.lutalic.smartdvr.data.NeuroSkyRepository
import com.lutalic.smartdvr.data.MedianStatisticsRepository
import com.lutalic.smartdvr.domain.BrainRepository
import com.lutalic.smartdvr.domain.StatisticsRepository
import org.koin.dsl.module

val dataModule = module {

    single<StatisticsRepository> {
        MedianStatisticsRepository()
    }

    single<BrainRepository> {
        NeuroSkyRepository(
            statisticsRepository = get()
        )
    }

}