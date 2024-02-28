package com.crow.base.app

import android.content.Context
import android.content.Intent
import com.crow.base.tools.extensions.error
import com.crow.base.tools.extensions.toJson
import com.crow.base.ui.activity.CrashActivity
import kotlin.system.exitProcess


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/cn/barry/base/app
 * @Time: 2023/8/2 12:00 PM
 * @Author: CrowForKotlin
 * @Description: BaseAppExceptionHandler
 **************************/
class BaseAppExceptionHandler private constructor(
    private val mApplicationContext: Context,
    private val mDefaultHandler: Thread.UncaughtExceptionHandler,
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        runCatching {
            throwable.stackTraceToString().error()
            launchActivity(mApplicationContext, CrashActivity::class.java, throwable)
            exitProcess(0)
        }
            .onSuccess { "Caught Global Exception !!!".error() }
            .onFailure { mDefaultHandler.uncaughtException(thread, it) }
    }

    companion object {

        private const val INTENT_EXTRA = "Throwable"

        fun initialize(applicationContext: Context) {
            Thread.setDefaultUncaughtExceptionHandler(
                BaseAppExceptionHandler(
                    applicationContext,
                    Thread.getDefaultUncaughtExceptionHandler() as Thread.UncaughtExceptionHandler,
                )
            )
        }

        fun getThrowableFromIntent(intent: Intent): Throwable? {
            return runCatching { Throwable(intent.getStringExtra(INTENT_EXTRA)!!) }
                .onFailure { "Wasn't able to retrive throwable from intent".error() }
                .getOrNull()
        }
    }

    private fun launchActivity(
        applicationContext: Context,
        activity: Class<*>,
        exception: Throwable
    ) {
        applicationContext.startActivity(Intent(applicationContext, activity).apply {
            putExtra(INTENT_EXTRA, toJson(exception.stackTraceToString()))
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
    }
}