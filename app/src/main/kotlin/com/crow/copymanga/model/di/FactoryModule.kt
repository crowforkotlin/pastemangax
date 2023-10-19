package com.crow.copymanga.model.di

import com.crow.module_anime.network.AnimeRepository
import com.crow.module_book.network.BookRepository
import com.crow.module_bookshelf.network.BookShelfRepository
import com.crow.module_discover.network.DiscoverRepository
import com.crow.module_home.network.HomeRepository
import com.crow.module_main.network.AppRepository
import com.crow.module_main.network.MainRepository
import com.crow.module_mine.network.MineRepository
import org.koin.dsl.module

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: app/src/main/java/com/ssti/laser/app/module
 * @Time: 2022/5/29 1:48
 * @Author: CrowForKotlin
 * @Description: RepositoryModule
 * @formatter:off
 **************************/

val factoryModule = module {
    factory { HomeRepository(get()) }
    factory { AppRepository(get()) }
    factory { MainRepository(get()) }
    factory { BookRepository(get()) }
    factory { MineRepository(get()) }
    factory { BookShelfRepository(get()) }
    factory { DiscoverRepository(get()) }
     factory { AnimeRepository(get()) }
}