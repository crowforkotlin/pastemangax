
package com.crow.base.copymanga.entity

import com.crow.base.app.app
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUserConfig
import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.appConfigDataStore
import com.crow.base.tools.extensions.asyncDecode
import com.crow.base.tools.extensions.asyncEncode
import com.crow.base.tools.extensions.decode
import com.crow.base.tools.extensions.toJson
import com.crow.base.tools.extensions.toTypeEntity
import com.squareup.moshi.Json

data class AppConfigEntity(

    /** ● 第一次初始化 */
    @Json(name = "App_FirstInit")
    val mAppFirstInit: Boolean = false,

    /** ● 站点 */
    @Json(name = "Site")
    val mSite: String = BaseStrings.URL.COPYMANGA,

    /** ● 路线 "0", "1" */
    @Json(name = "Route")
    val mRoute: String = BaseUserConfig.CURRENT_ROUTE,

    /**
     * ● 分辨率 800、1200、1500
     *
     * ● 2023-10-02 23:36:24 周一 下午
     */
    @Json(name = "Resolution")
    val mResolution: Int = BaseUserConfig.RESOLUTION
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