
package com.crow.mangax.copymanga.entity

import com.crow.base.app.app
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.BaseUserConfig
import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.appConfigDataStore
import com.crow.base.tools.extensions.asyncDecode
import com.crow.base.tools.extensions.asyncEncode
import com.crow.base.tools.extensions.decode
import com.crow.base.tools.extensions.toJson
import com.crow.base.tools.extensions.toTypeEntity
import com.squareup.moshi.Json

/**
 * App 全局配置
 *
 * @property mAppFirstInit 第一次初始化
 * @property mHotMangaSite HotManga站点
 * @property mCopyMangaSite CopyManga站点
 * @property mRoute 路线 "0", "1"
 * @property mResolution 分辨率 800、1200、1500
 * @constructor Create empty App config entity
 */
data class AppConfigEntity(

    @Json(name = "App_FirstInit")
    val mAppFirstInit: Boolean = false,

    @Json(name = "HotManga_Site")
    val mHotMangaSite: String = BaseStrings.URL.HotManga,

    @Json(name = "CopyManga_Site")
    val mCopyMangaSite: String = BaseStrings.URL.COPYMANGA,

    @Json(name = "Route")
    val mRoute: String = BaseUserConfig.CURRENT_ROUTE,

    @Json(name = "Resolution")
    val mResolution: Int = BaseUserConfig.RESOLUTION,
) {
    companion object {

        private var mAppConfigEntity: AppConfigEntity? =null

        fun getInstance(): AppConfigEntity {
            return mAppConfigEntity!!
        }

        suspend fun saveAppConfig(appConfigEntity: AppConfigEntity) {
            mAppConfigEntity = appConfigEntity
            app.appConfigDataStore.asyncEncode(DataStoreAgent.APP_CONFIG, toJson(appConfigEntity))
        }

        suspend fun readAppConfig(): AppConfigEntity? {
            return  toTypeEntity<AppConfigEntity>(app.appConfigDataStore.asyncDecode(DataStoreAgent.APP_CONFIG)).also { mAppConfigEntity = it }
        }

        fun readAppConfigSync(): AppConfigEntity? {
            return toTypeEntity<AppConfigEntity>(app.appConfigDataStore.decode(DataStoreAgent.APP_CONFIG)).also { mAppConfigEntity = it }
        }
    }
}