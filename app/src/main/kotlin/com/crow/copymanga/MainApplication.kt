package com.crow.copymanga

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.crow.base.app.BaseApp
import com.crow.copymanga.di.factoryModule
import com.crow.copymanga.di.netWorkModule
import com.crow.copymanga.di.servicesModule
import com.crow.copymanga.di.viewModelModule
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: app/src/main/java/com/crow/interview
 * @Time: 2022/12/29 21:44
 * @Author: BarryAllen
 * @Description: MyApplication
 * @formatter:on
 **************************/
class MainApplication : BaseApp() {


    override fun onCreate() {
        super.onCreate()

        val strategy = UserStrategy(applicationContext)

        strategy.deviceID = "CrowForKotlin";
        strategy.deviceModel = "TestUnit"
        strategy.appChannel = "MyChannel"
        strategy.appVersion = "1.0.0"
        strategy.appPackageName = "com.crow.copymanga"
        strategy.appReportDelay = 10000

        CrashReport.initCrashReport(applicationContext, "b848968d52", false, strategy);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)



        startKoin {
            androidContext(this@MainApplication)
            modules(listOf(netWorkModule, servicesModule, viewModelModule, factoryModule))
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}