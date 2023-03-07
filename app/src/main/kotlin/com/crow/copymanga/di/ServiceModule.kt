package com.crow.copymanga.di

import com.crow.module_home.network.HomeService
import org.koin.dsl.module
import retrofit2.Retrofit

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: app/src/main/java/com/ssti/laser/app/module
 * @Time: 2022/5/29 1:49
 * @Author: BarryAllen
 * @Description: ServiceModule
 * @formatter:off
 **************************/

val servicesModule = module {
    single { get<Retrofit>().create(HomeService::class.java) }
}