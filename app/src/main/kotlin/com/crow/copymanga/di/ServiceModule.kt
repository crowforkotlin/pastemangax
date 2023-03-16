package com.crow.copymanga.di

import com.crow.module_comic.network.ComicService
import com.crow.module_home.network.HomeService
import com.crow.module_main.network.ContainerService
import org.koin.dsl.module
import retrofit2.Retrofit

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: app/src/main/java/com/ssti/laser/app/module
 * @Time: 2022/5/29 1:49
 * @Author: CrowForKotlin
 * @Description: ServiceModule
 * @formatter:off
 **************************/

val servicesModule = module {
    single { get<Retrofit>().create(HomeService::class.java) }
    single { get<Retrofit>().create(ContainerService::class.java) }
    single { get<Retrofit>().create(ComicService::class.java) }
}