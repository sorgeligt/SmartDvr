package com.lutalic.smartdvr.di

import com.lutalic.smartdvr.presentation.graph.GraphViewModel
import com.lutalic.smartdvr.presentation.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel<MainViewModel> {
        MainViewModel(
            brainRepository = get()
        )
    }

    viewModel<GraphViewModel> {
        GraphViewModel(
            brainRepository = get()
        )
    }
}