package com.crow.base.copymanga.entity

import androidx.appcompat.app.AppCompatDelegate
import com.crow.base.app.appContext
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUser
import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.appConfigDataStore
import com.crow.base.tools.extensions.asyncDecode
import com.crow.base.tools.extensions.asyncEncode
import com.crow.base.tools.extensions.toJson
import com.crow.base.tools.extensions.toTypeEntity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppConfigEntity(

    /** ● 第一次初始化 */
    val mAppFirstInit: Boolean = false,

    /** ● 站点 */
    val mSite: String = BaseStrings.URL.CopyManga,

    /** ● 路线 "0", "1" */
    val mRoute: String = BaseUser.CURRENT_ROUTE,

    /** ● 黑夜模式 */
    val mDarkMode: Int = AppCompatDelegate.MODE_NIGHT_NO
) {
    companion object {
        suspend fun saveAppConfig(appConfigEntity: AppConfigEntity) {
            appContext.appConfigDataStore.asyncEncode(DataStoreAgent.APP_CONFIG, toJson(appConfigEntity))
        }

        suspend fun readAppConfig(): AppConfigEntity? {
            return appContext.appConfigDataStore.asyncDecode(DataStoreAgent.APP_CONFIG).toTypeEntity<AppConfigEntity>()
        }
    }
}
