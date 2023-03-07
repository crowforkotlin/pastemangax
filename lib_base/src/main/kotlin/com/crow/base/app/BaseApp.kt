package com.crow.base.app

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

/*************************
 * @Machine: RedmiBook Pro 15
 * @RelativePath: com\barry\base\app\BaseApp.kt
 * @Path: D:\Barry\B_study\Android\2022\OrchardAssistant\lib_base\src\main\java\com\barry\base\app\BaseApp.kt
 * @Author: Barry
 * @Time: 2022/2/20 03:12 凌晨
 * @Description:
 *************************/

val appContext = BaseApp.context

open class BaseApp : Application() {

    companion object { lateinit var context: Application }

    override fun onCreate() {
        super.onCreate()
        context = this
        Logger.addLogAdapter(AndroidLogAdapter())
        Thread.setDefaultUncaughtExceptionHandler(BaseAppException())
    }
}