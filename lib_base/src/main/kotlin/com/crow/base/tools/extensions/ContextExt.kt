package com.crow.base.tools.extensions

import android.content.Context
import android.content.res.Resources
import androidx.annotation.Px
import java.io.File
import kotlin.math.roundToInt

fun Context.createFileInCacheDir(name: String): File {
    val file = File(externalCacheDir, name)
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    return file
}

@Px
fun Resources.resolveDp(dp: Int) = (dp * displayMetrics.density).roundToInt()

@Px
fun Resources.resolveDp(dp: Float) = dp * displayMetrics.density
