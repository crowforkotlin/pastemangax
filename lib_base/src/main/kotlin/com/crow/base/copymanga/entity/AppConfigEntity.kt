
package com.crow.base.copymanga.entity

import com.crow.base.app.appContext
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUser
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
    val mRoute: String = BaseUser.CURRENT_ROUTE,


    ) {
    companion object {

        private var mAppConfigEntity: AppConfigEntity? =null

        fun getInstance(): AppConfigEntity {
            return mAppConfigEntity!!
        }

        suspend fun saveAppConfig(appConfigEntity: AppConfigEntity) {
            mAppConfigEntity = appConfigEntity
            appContext.appConfigDataStore.asyncEncode(DataStoreAgent.APP_CONFIG, toJson(appConfigEntity))
        }

        suspend fun readAppConfig(): AppConfigEntity? {
            return  toTypeEntity<AppConfigEntity>(appContext.appConfigDataStore.asyncDecode(DataStoreAgent.APP_CONFIG)).also { mAppConfigEntity = it }
        }

        fun readAppConfigSync(): AppConfigEntity? {
            return toTypeEntity<AppConfigEntity>(appContext.appConfigDataStore.decode(DataStoreAgent.APP_CONFIG)).also { mAppConfigEntity = it }
        }
    }
}