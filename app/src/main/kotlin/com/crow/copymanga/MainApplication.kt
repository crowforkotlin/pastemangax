package com.crow.copymanga

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.crow.base.app.BaseApp
import com.crow.base.tools.extensions.log
import com.crow.mangax.copymanga.entity.AppConfigEntity.Companion.mDarkMode
import com.crow.copymanga.model.di.factoryModule
import com.crow.copymanga.model.di.fragmentModule
import com.crow.copymanga.model.di.networkModule
import com.crow.copymanga.model.di.servicesModule
import com.crow.copymanga.model.di.singleModule
import com.crow.copymanga.model.di.viewModelModule
import com.crow.mangax.copymanga.entity.AppConfigEntity
import com.crow.mangax.tools.language.ChineseConverter
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.core.context.startKoin
import kotlin.system.measureTimeMillis


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

        AppConfigEntity.initialization()

        AppCompatDelegate.setDefaultNightMode(if(mDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

        ChineseConverter.initialize(applicationContext)

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