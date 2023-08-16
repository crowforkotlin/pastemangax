package com.crow.base.ui.compose

import android.content.Context
import android.os.Build
import com.crow.base.BuildConfig
import com.crow.base.tools.coroutine.withNonCancellableContext
import com.crow.base.tools.coroutine.withUIContext
import com.crow.base.tools.extensions.createFileInCacheDir
import com.crow.base.tools.extensions.getUriCompat
import com.crow.base.tools.extensions.toShareIntent
import com.crow.base.tools.extensions.toast

class CrashLogUtil(private val context: Context) {

    suspend fun dumpLogs() = withNonCancellableContext {
        runCatching {
            val file = context.createFileInCacheDir("tachiyomi_crash_logs.txt")
            Runtime.getRuntime().exec("logcat *:E -d -f ${file.absolutePath}").waitFor()
            file.appendText(getDebugInfo())

            val uri = file.getUriCompat(context)
            context.startActivity(uri.toShareIntent(context, "text/plain"))
        }
            .onFailure {
                withUIContext { toast("Failed to get logs") }
            }
    }

    fun getDebugInfo(): String {
        return """
            App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
            Android version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            Android build ID: ${Build.DISPLAY}
            Device brand: ${Build.BRAND}
            Device manufacturer: ${Build.MANUFACTURER}
            Device name: ${Build.DEVICE}
            Device model: ${Build.MODEL}
            Device product name: ${Build.PRODUCT}
        """.trimIndent()
    }
}
