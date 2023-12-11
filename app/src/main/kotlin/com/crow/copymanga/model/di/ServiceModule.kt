package com.crow.copymanga.model.di

import com.crow.module_anime.network.AnimeService
import com.crow.module_book.network.ComicService
import com.crow.module_bookshelf.network.BookShelfService
import com.crow.module_discover.network.DiscoverService
import com.crow.module_home.network.HomeService
import com.crow.module_main.network.AppService
import com.crow.module_main.network.MainService
import com.crow.module_mine.network.MineService
import org.koin.core.qualifier.named
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
    val named_CopyMangaX = named("CopyMangaX")
    val named_HotMangaX = named("HotMangaX")
    single { get<Retrofit>().create(AppService::class.java) }
    single { get<Retrofit>(named_CopyMangaX).create(MainService::class.java) }
    single { get<Retrofit>(named_CopyMangaX).create(HomeService::class.java) }
    single { get<Retrofit>(named_CopyMangaX).create(ComicService::class.java) }
    single { get<Retrofit>(named_CopyMangaX).create(MineService::class.java) }
    single { get<Retrofit>(named_CopyMangaX).create(BookShelfService::class.java) }
    single { get<Retrofit>(named_CopyMangaX).create(DiscoverService::class.java) }
    single { get<Retrofit>(named_HotMangaX).create(AnimeService::class.java) }
}