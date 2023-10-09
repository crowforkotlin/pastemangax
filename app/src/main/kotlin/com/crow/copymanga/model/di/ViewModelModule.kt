package com.crow.copymanga.model.di

import com.crow.module_anime.ui.viewmodel.AnimeViewModel
import com.crow.module_book.ui.viewmodel.BookViewModel
import com.crow.module_book.ui.viewmodel.ComicViewModel
import com.crow.module_bookshelf.ui.viewmodel.BookshelfViewModel
import com.crow.module_discover.ui.viewmodel.DiscoverViewModel
import com.crow.module_home.ui.viewmodel.HomeViewModel
import com.crow.module_main.ui.viewmodel.HistoryViewModel
import com.crow.module_main.ui.viewmodel.ImageViewModel
import com.crow.module_main.ui.viewmodel.MainViewModel
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
    viewModel { MainViewModel(get()) }
    viewModel { DiscoverViewModel(get()) }
    viewModel { BookViewModel(get()) }
    viewModel { UserViewModel(get()) }
    viewModel { ComicViewModel(get()) }
    viewModel { UserInfoViewModel() }
    viewModel { ImageViewModel() }
    viewModel { HistoryViewModel(get()) }
    viewModel { AnimeViewModel(get()) }
}