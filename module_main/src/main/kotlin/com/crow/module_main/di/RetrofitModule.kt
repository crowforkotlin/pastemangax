package com.crow.module_main.di

import com.crow.base.current_project.BaseStrings
import com.crow.base.tools.extensions.baseMoshi
import com.crow.base.tools.network.FlowCallAdapterFactory
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: app/src/main/java/com/ssti/laser/app/module
 * @Time: 2022/5/29 1:48
 * @Author: CrowForKotlin
 * @Description: NetWorkModule
 * @formatter:off
 **************************/

val retrofitModule = module {
    single {
        
        Retrofit.Builder()
            .baseUrl(BaseStrings.URL.CopyManga)
            .client(get())
            .addCallAdapterFactory(FlowCallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(baseMoshi))
            .build()
    }
}