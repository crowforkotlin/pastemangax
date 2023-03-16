package com.crow.copymanga.di

import com.crow.module_comic.network.ComicRepository
import com.crow.module_home.model.factory.HomeRepository
import com.crow.module_main.network.ContainerRepository
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
    factory { ContainerRepository(get()) }
    factory { ComicRepository(get()) }
}