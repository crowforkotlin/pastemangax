package com.crow.module_main.ui.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.crow.base.copymanga.appIsDarkMode
import com.crow.base.copymanga.entity.AppConfigEntity
import com.crow.base.tools.extensions.SpNameSpace
import com.crow.base.tools.extensions.getSharedPreferences
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_main.model.intent.MainIntent
import com.crow.module_main.network.ContainerRepository
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
class MainViewModel(val repository: ContainerRepository) : BaseMviViewModel<MainIntent>() {

    /** ● app配置 设置粘性状态 （内部访问）*/
    private var _mAppConfig = MutableStateFlow<AppConfigEntity?>(null)

    /** ● app配置 设置粘性状态 （ 公开）*/
    val mAppConfig: StateFlow<AppConfigEntity?> get() = _mAppConfig

    /** ● 是否重启（内存重启、旋转、夜间模式切换） */
    var mIsRestarted: Boolean = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _mAppConfig.value = AppConfigEntity.readAppConfig() ?: AppConfigEntity(true)
        }
    }

    fun saveAppConfig(appConfigEntity: AppConfigEntity = AppConfigEntity()) {
        viewModelScope.launch { AppConfigEntity.saveAppConfig(appConfigEntity) }
    }

    fun saveCatalogDarkModeEnable(darkMode: Int) {
        appIsDarkMode = darkMode == AppCompatDelegate.MODE_NIGHT_YES
        SpNameSpace.CATALOG_NIGHT_MODE.getSharedPreferences().edit { putBoolean(SpNameSpace.Key.ENABLE_DARK, appIsDarkMode) }
    }

    suspend fun getReadedAppConfig(): AppConfigEntity? {
        return suspendCancellableCoroutine { continuation ->
            viewModelScope.launch {
                runCatching { continuation.resume(AppConfigEntity.readAppConfig()) }.onFailure { continuation.resume(null) }
            }
        }
    }

    private fun getUpdateInfo(intent: MainIntent.GetUpdateInfo) {
        flowResult(intent, repository.getUpdateInfo()) { value -> intent.copy(appUpdateResp = value) }
    }

    private fun getQQGropu(intent: MainIntent.GetQQGroup) {
        flowResult(intent, repository.getQQGroup()) { value -> intent.copy(link = value.string()) }
    }

    private fun getSite(intent: MainIntent.GetDynamicSite) {
        flowResult(intent, repository.getSite()) { value -> intent.copy(siteResp = value) }
    }

    override fun dispatcher(intent: MainIntent) {
        when(intent) {
            is MainIntent.GetUpdateInfo -> getUpdateInfo(intent)
            is MainIntent.GetQQGroup -> getQQGropu(intent)
            is MainIntent.GetDynamicSite -> getSite(intent)
        }
    }
}