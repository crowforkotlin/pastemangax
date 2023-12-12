package com.crow.copymanga

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.crow.base.app.BaseApp
import com.crow.mangax.copymanga.appIsDarkMode
import com.crow.copymanga.model.di.factoryModule
import com.crow.copymanga.model.di.fragmentModule
import com.crow.copymanga.model.di.networkModule
import com.crow.copymanga.model.di.servicesModule
import com.crow.copymanga.model.di.singleModule
import com.crow.copymanga.model.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.core.context.startKoin


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: app/src/main/java/com/crow/interview
 * @Time: 2022/12/29 21:44
 * @Author: CrowForKotlin
 * @Description: MyApplication
 * @formatter:on
 **************************/
class MainApplication : BaseApp() {

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(if(appIsDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

        startKoin {
            fragmentFactory()
            androidContext(this@MainApplication)
            modules(
                listOf(
                    singleModule,
                    networkModule,
                    servicesModule,
                    viewModelModule,
                    factoryModule,
                    fragmentModule
                )
            )
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}