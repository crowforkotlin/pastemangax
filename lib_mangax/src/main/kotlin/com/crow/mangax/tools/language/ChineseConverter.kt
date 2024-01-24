@file:Suppress("SameParameterValue")

package com.crow.mangax.tools.language

import android.content.Context
import com.crow.base.app.app
import com.crow.base.tools.extensions.copyFolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors

/**
 * ● 中文转化工具
 *
 * ● 2023-12-14 01:51:12 周四 上午
 * @author crowforkotlin
 */
object ChineseConverter {

    /**
     * ● 转换协程作用域
     *
     * ● 2023-12-14 01:44:12 周四 上午
     * @author crowforkotlin
     */
    private val mConvertScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    /**
     * ● 加载动态库
     *
     * ● 2023-12-14 01:46:59 周四 上午
     * @author crowforkotlin
     */
    init { System.loadLibrary("ChineseConverter") }

    /**
     * ● 初始化词典文件夹
     *
     * ● 2023-12-14 01:49:17 周四 上午
     * @author crowforkotlin
     */
    fun initialize(context: Context) {
        mConvertScope.launch(Dispatchers.IO) {
            val lastDataFile = File(app.filesDir.toString() + "/opencc_data/zFinished2")
            if (!lastDataFile.exists()) {
                initialize(app)
            }
            context.copyFolder("opencc_data")
        }
    }

    /**
     * ● 清除词典数据文件夹，仅在更新词典数据时调用该方法。
     *
     * ● 2023-12-14 01:48:27 周四 上午
     * @author crowforkotlin
     */
    fun clearDictDataFolder(context: Context) {
        File(context.filesDir.toString(), "opencc_data").deleteRecursively()
    }

    /**
     * ● 转换文本
     *
     * ● 2023-12-14 01:47:43 周四 上午
     * @author crowforkotlin
     */
    suspend fun convert(text: String, conversionType: ConversionType = ConversionType.HK2S): String {
        return mConvertScope.async { nativeConvert(text, "${app.filesDir.absolutePath}/opencc_data/${conversionType.value}") }.await()
    }

    private external fun convert(text: String, configFile: String, absoluteDataFolderPath: String): String

    private external fun nativeConvert(text: String, filePath: String): String

    fun cancel() {
       mConvertScope.cancel()
    }
}