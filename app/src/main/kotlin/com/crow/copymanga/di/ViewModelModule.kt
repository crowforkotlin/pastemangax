package com.crow.copymanga.di

import com.crow.module_bookshelf.ui.viewmodel.BookshelfViewModel
import com.crow.module_comic.ui.viewmodel.BookInfoViewModel
import com.crow.module_discovery.ui.viewmodel.DiscoveryViewModel
import com.crow.module_home.ui.viewmodel.HomeViewModel
import com.crow.module_main.ui.viewmodel.ContainerViewModel
import com.crow.module_user.ui.viewmodel.UserInfoViewModel
import com.crow.module_user.ui.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: app/src/main/java/com/ssti/laser/app/module
 * @Time: 2022/5/29 1:47
 * @Author: CrowForKotlin
 * @Description: ViewModelModule
 * @formatter:off
 **************************/

val viewModelModule = module {

    viewModel { HomeViewModel(get()) }
    viewModel { BookshelfViewModel(get()) }
    viewModel { ContainerViewModel(get()) }
    viewModel { DiscoveryViewModel() }
    viewModel { BookInfoViewModel(get()) }
    viewModel { UserViewModel(get()) }
    viewModel { UserInfoViewModel() }

}