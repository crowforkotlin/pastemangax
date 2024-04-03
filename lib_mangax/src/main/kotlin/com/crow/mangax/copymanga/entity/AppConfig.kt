
package com.crow.mangax.copymanga.entity

import android.content.SharedPreferences
import androidx.core.content.edit
import com.crow.base.app.app
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.SpNameSpace
import com.crow.base.tools.extensions.appConfigDataStore
import com.crow.base.tools.extensions.asyncDecode
import com.crow.base.tools.extensions.asyncEncode
import com.crow.base.tools.extensions.decode
import com.crow.base.tools.extensions.getSharedPreferences
import com.crow.base.tools.extensions.toJson
import com.crow.base.tools.extensions.toTypeEntity
import com.squareup.moshi.Json
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch



/**
 * App 全局配置
 *
 * @property mAppFirstInit 第一次初始化
 * @property mHotMangaSite HotManga站点
 * @property mCopyMangaSite CopyManga站点
 * @property mRoute 路线 "0", "1"
 * @property mResolution 分辨率 800、1200、1500
 * @property mNoticeVersion 公告通知版本
 * @constructor Create empty App config entity
 */

data class AppConfig(

    @Json(name = "App_FirstInit")
    val mAppFirstInit: Boolean = false,

    @Json(name = "HotManga_Site")
    val mHotMangaSite: String = BaseStrings.URL.HotManga,

    @Json(name = "CopyManga_Site")
    val mCopyMangaSite: String = BaseStrings.URL.COPYMANGA,

    @Json(name = "Route")
    val mRoute: String = MangaXAccountConfig.mRoute,

    @Json(name = "Resolution")
    val mResolution: Int = MangaXAccountConfig.mResolution,

    @Json(name = "ApiSecret")
    val mApiSecret: String? = null,

    @Json(name = "NoticeVersion")
    val mNoticeVersion: Long = 0
) {
    companion object {

        private var mAppConfig: AppConfig? = null

        fun getInstance(): AppConfig? { return mAppConfig }

        suspend fun saveAppConfig(appConfig: AppConfig) {
            app.appConfigDataStore.asyncEncode(DataStoreAgent.APP_CONFIG, toJson(appConfig.also { mAppConfig = it }))
        }

        suspend fun readAppConfig(): AppConfig? {
            return  toTypeEntity<AppConfig>(app.appConfigDataStore.asyncDecode(DataStoreAgent.APP_CONFIG)).also { mAppConfig = it }
        }

        fun readAppConfigSync(): AppConfig? {
            return toTypeEntity<AppConfig>(app.appConfigDataStore.decode(DataStoreAgent.APP_CONFIG)).also { mAppConfig = it }
        }
    }
}