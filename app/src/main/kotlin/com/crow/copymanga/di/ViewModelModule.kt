package com.crow.copymanga.di

import com.crow.module_bookshelf.BookShelfViewModel
import com.crow.module_home.ui.HomeViewModel
import com.crow.module_main.viewmodel.ContainerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: app/src/main/java/com/ssti/laser/app/module
 * @Time: 2022/5/29 1:47
 * @Author: BarryAllen
 * @Description: ViewModelModule
 * @formatter:off
 **************************/

val viewModelModule = module {

    viewModel { HomeViewModel(get()) }
    viewModel { BookShelfViewModel() }
    viewModel { ContainerViewModel() }

}