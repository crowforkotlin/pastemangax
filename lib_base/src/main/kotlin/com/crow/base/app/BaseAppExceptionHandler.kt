package com.crow.base.app

import android.content.Context
import android.content.Intent
import com.crow.base.tools.extensions.logError
import com.crow.base.tools.extensions.toJson
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
    private val mActivityToBeLaunched: Class<*>,
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        runCatching {
            throwable.stackTraceToString().logError()
            launchActivity(mApplicationContext, mActivityToBeLaunched, throwable)
            exitProcess(0)
        }
            .onSuccess { "Caught Global Exception !!!".logError() }
            .onFailure { mDefaultHandler.uncaughtException(thread, it) }
    }

    companion object {

        private const val INTENT_EXTRA = "Throwable"

        fun initialize(applicationContext: Context, activityToBeLaunched: Class<*>) {
            Thread.setDefaultUncaughtExceptionHandler(
                BaseAppExceptionHandler(
                    applicationContext,
                    Thread.getDefaultUncaughtExceptionHandler() as Thread.UncaughtExceptionHandler,
                    activityToBeLaunched
                )
            )
        }

        fun getThrowableFromIntent(intent: Intent): Throwable? {
            return runCatching { Throwable(intent.getStringExtra(INTENT_EXTRA)!!) }
                .onFailure { "Wasn't able to retrive throwable from intent".logError() }
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