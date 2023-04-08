package com.crow.copymanga.di

import com.crow.module_bookshelf.network.BookShelfService
import com.crow.module_book.network.ComicService
import com.crow.module_discover.network.DiscoverService
import com.crow.module_home.network.HomeService
import com.crow.module_main.network.ContainerService
import com.crow.module_user.network.UserService
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
    single { get<Retrofit>().create(UserService::class.java) }
    single { get<Retrofit>().create(BookShelfService::class.java) }
    single { get<Retrofit>().create(DiscoverService::class.java) }
}