package com.crow.module_main.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.crow.base.copymanga.entity.AppConfigEntity
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_main.model.intent.ContainerIntent
import com.crow.module_main.network.ContainerRepository
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
class ContainerViewModel(val repository: ContainerRepository) : BaseMviViewModel<ContainerIntent>() {

    // app配置 设置粘性状态
    private var _appConfig = MutableStateFlow<AppConfigEntity?>(null)
    val appConfig: StateFlow<AppConfigEntity?> get() = _appConfig

    init {
        viewModelScope.launch {
            _appConfig.value = AppConfigEntity.readAppConfig() ?: AppConfigEntity(true)
        }
    }

    fun saveAppConfig(appConfigEntity: AppConfigEntity = AppConfigEntity()) {
        viewModelScope.launch { AppConfigEntity.saveAppConfig(appConfigEntity) }
    }

    suspend fun getReadedAppConfig(): AppConfigEntity? {
        return suspendCancellableCoroutine { continuation ->
            viewModelScope.launch {
                runCatching { continuation.resume(AppConfigEntity.readAppConfig()) }.onFailure { continuation.resume(null) }
            }
        }
    }

    private fun getUpdateInfo(intent: ContainerIntent.GetUpdateInfo) {
        flowResult(intent, repository.getUpdateInfo()) { value -> intent.copy(appUpdateResp = value) }
    }

    private fun getQQGropu(intent: ContainerIntent.GetQQGroup) {
        flowResult(intent, repository.getQQGroup()) { value -> intent.copy(link = value.string()) }
    }

    private fun getSite(intent: ContainerIntent.GetDynamicSite) {
        flowResult(intent, repository.getSite()) { value -> intent.copy(siteResp = value) }
    }

    override fun dispatcher(intent: ContainerIntent) {
        when(intent) {
            is ContainerIntent.GetUpdateInfo -> getUpdateInfo(intent)
            is ContainerIntent.GetQQGroup -> getQQGropu(intent)
            is ContainerIntent.GetDynamicSite -> getSite(intent)
        }
    }
}