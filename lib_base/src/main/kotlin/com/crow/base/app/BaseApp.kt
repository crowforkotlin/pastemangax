package com.crow.base.app

import android.app.Application
import com.crow.base.ui.activity.CrashActivity
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

val appContext = BaseApp.context

open class BaseApp : Application() {

    companion object { lateinit var context: Application }

    override fun onCreate() {
        super.onCreate()
        context = this
        Logger.addLogAdapter(AndroidLogAdapter())
        BaseAppExceptionHandler.initialize(applicationContext, CrashActivity::class.java)
    }
}