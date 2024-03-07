
package com.crow.mangax.copymanga.entity

import android.content.SharedPreferences
import androidx.core.content.edit
import com.crow.base.tools.extensions.SpNameSpace
import com.crow.base.tools.extensions.getSharedPreferences
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

object CatlogConfig {

    /**
     * ⦁  黑夜模式
     *
     * ⦁ 2023-12-18 00:10:11 周一 上午
     * @author crowforkotlin
     */
    var mDarkMode = false

    /**
     * ⦁ 更新前置
     *
     * ⦁ 2023-12-18 00:10:25 周一 上午
     * @author crowforkotlin
     */
    var mUpdatePrefix = true
        private set

    /**
     * ⦁ 简繁题转换
     *
     * ⦁ 2023-12-18 00:10:38 周一 上午
     * @author crowforkotlin
     */
    var mChineseConvert = true
        private set

    /**
     * ⦁ 热度精准显示
     *
     * ⦁ 2023-12-18 00:10:53 周一 上午
     * @author crowforkotlin
     */
    var mHotAccurateDisplay = false
        private set

    /**
     * ⦁  封面原图
     *
     * ⦁ 2024-03-06 20:35:49 周三 下午
     * @author crowforkotlin
     */
    var mCoverOrinal = false
        private set

    /**
     * ⦁ API代理是否启用
     *
     * ⦁ 2024-03-07 22:02:43 周四 下午
     * @author crowforkotlin
     */
    var mApiProxyEnable = false


    @OptIn(DelicateCoroutinesApi::class)
    suspend fun initialization(sp: SharedPreferences) {
        withContext(coroutineContext) {
            mChineseConvert = sp.getBoolean(SpNameSpace.Key.ENABLE_CHINESE_CONVERT, true)
            mHotAccurateDisplay = sp.getBoolean(SpNameSpace.Key.ENABLE_HOT_ACCURATE_DISPLAY, false)
            mUpdatePrefix = sp.getBoolean(SpNameSpace.Key.ENABLE_UPDATE_PREFIX, true)
            mCoverOrinal = sp.getBoolean(SpNameSpace.Key.ENABLE_COVER_ORINAL, false)
            mApiProxyEnable = sp.getBoolean(SpNameSpace.Key.ENABLE_API_PROXY, false)
        }
    }

    fun saveCatlogConfig(key: String, value: Boolean) { SpNameSpace.CATALOG_CONFIG.getSharedPreferences().edit { putBoolean(key, value) } }

    fun getCatlogConfigSp() = SpNameSpace.CATALOG_CONFIG.getSharedPreferences()
}