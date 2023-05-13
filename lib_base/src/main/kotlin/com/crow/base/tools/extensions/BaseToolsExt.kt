package com.crow.base.tools.extensions

import android.content.pm.PackageManager
import android.os.Build
import com.crow.base.app.appContext

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/kotlin/com/crow/base/tools/extensions
 * @Time: 2023/4/7 14:53
 * @Author: CrowForKotlin
 * @Description: BaseToolsExt
 * @formatter:on
 **************************/

fun isLatestVersion(current: Long = getCurrentVersionCode(), latest: Long): Boolean = current >= latest

fun getCurrentVersionCode(): Long {
    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        appContext.packageManager.getPackageInfo(appContext.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        appContext.packageManager.getPackageInfo(appContext.packageName, 0)
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    } else {
        packageInfo.versionCode.toLong()
    }
}

fun getCurrentVersionName(): String {
    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        appContext.packageManager.getPackageInfo(appContext.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        appContext.packageManager.getPackageInfo(appContext.packageName, 0)
    }
    return packageInfo.versionName
}