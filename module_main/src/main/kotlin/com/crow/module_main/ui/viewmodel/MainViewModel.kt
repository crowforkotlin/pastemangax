package com.crow.module_main.ui.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.crow.mangax.copymanga.entity.AppConfig.Companion.mDarkMode
import com.crow.mangax.copymanga.entity.AppConfig
import com.crow.base.tools.extensions.SpNameSpace
import com.crow.base.tools.extensions.getSharedPreferences
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.mangax.copymanga.BaseStrings
import com.crow.mangax.copymanga.MangaXAccountConfig
import com.crow.module_main.model.intent.AppIntent
import com.crow.module_main.network.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_main/src/main/kotlin/com/crow/module_main/ui
 * @Time: 2023/3/7 23:56
 * @Author: CrowForKotlin
 * @Description: ContainerViewModel
 * @formatter:on
 **************************/
class MainViewModel(val repository: AppRepository) : BaseMviViewModel<AppIntent>() {

    /** ● app配置 设置粘性状态 （内部访问）*/
    private var _mAppConfig = MutableStateFlow<AppConfig?>(null)

    /** ● app配置 设置粘性状态 （ 公开）*/
    val mAppConfig: StateFlow<AppConfig?> get() = _mAppConfig

    /** ● 是否重启（内存重启、旋转、夜间模式切换） */
    var mIsRestarted: Boolean = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val config = AppConfig.readAppConfig() ?: AppConfig(true)
            BaseStrings.URL.COPYMANGA = config.mCopyMangaSite
            BaseStrings.URL.HotManga = config.mHotMangaSite
            MangaXAccountConfig.mRoute = config.mRoute
            MangaXAccountConfig.mResolution = config.mResolution
            _mAppConfig.value = config
        }
    }

    fun saveAppConfig(appConfig: AppConfig = AppConfig()) {
        viewModelScope.launch { AppConfig.saveAppConfig(appConfig) }
    }

    fun saveCatalogDarkModeEnable(darkMode: Int) {
        mDarkMode = darkMode == AppCompatDelegate.MODE_NIGHT_YES
        SpNameSpace.CATALOG_CONFIG.getSharedPreferences().edit { putBoolean(SpNameSpace.Key.ENABLE_DARK, mDarkMode) }
    }

    fun saveAppCatLogConfig(key: String, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            SpNameSpace.CATALOG_CONFIG.getSharedPreferences().edit { putBoolean(key, isChecked) }
        }
    }

    suspend fun getReadedAppConfig(): AppConfig? {
        return suspendCancellableCoroutine { continuation ->
            viewModelScope.launch {
                runCatching { continuation.resume(AppConfig.readAppConfig()) }.onFailure { continuation.resume(null) }
            }
        }
    }

    private fun getUpdateInfo(intent: AppIntent.GetUpdateInfo) {
        flowResult(intent, repository.getUpdateInfo()) { value -> intent.copy(appUpdateResp = value) }
    }

    private fun getQQGropu(intent: AppIntent.GetQQGroup) {
        flowResult(intent, repository.getQQGroup()) { value -> intent.copy(link = value.string()) }
    }

    private fun getSite(intent: AppIntent.GetDynamicSite) {
        flowResult(intent, repository.getSite()) { value -> intent.copy(siteResp = value) }
    }

    override fun dispatcher(intent: AppIntent) {
        when(intent) {
            is AppIntent.GetUpdateInfo -> getUpdateInfo(intent)
            is AppIntent.GetQQGroup -> getQQGropu(intent)
            is AppIntent.GetDynamicSite -> getSite(intent)
        }
    }
}