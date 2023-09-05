package com.crow.copymanga

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDex
import com.crow.base.app.BaseApp
import com.crow.base.tools.extensions.SpNameSpace
import com.crow.base.tools.extensions.getCurrentVersionName
import com.crow.base.tools.extensions.getSharedPreferences
import com.crow.copymanga.model.di.factoryModule
import com.crow.copymanga.model.di.fragmentModule
import com.crow.copymanga.model.di.networkModule
import com.crow.copymanga.model.di.servicesModule
import com.crow.copymanga.model.di.singleModule
import com.crow.copymanga.model.di.viewModelModule
import com.tencent.bugly.crashreport.CrashReport
import com.tencent.bugly.crashreport.CrashReport.UserStrategy
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
        AppCompatDelegate.setDefaultNightMode(if (SpNameSpace.CATALOG_NIGHT_MODE.getSharedPreferences().getBoolean(SpNameSpace.Key.ENABLE_DARK, false)) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

        val strategy = UserStrategy(applicationContext)
        strategy.deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        strategy.deviceModel = Build.MODEL
        strategy.appChannel = "Crow_Channel"
        strategy.appVersion = getCurrentVersionName()
        strategy.appPackageName = packageName
        strategy.appReportDelay = 10000


        CrashReport.initCrashReport(applicationContext, "b848968d52", false, strategy);

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