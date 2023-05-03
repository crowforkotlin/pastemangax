package com.crow.module_main.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.crow.base.app.appContext
import com.crow.base.tools.extensions.DataStoreAgent
import com.crow.base.tools.extensions.appConfigDataStore
import com.crow.base.tools.extensions.asyncDecode
import com.crow.base.tools.extensions.asyncEncode
import com.crow.base.tools.extensions.toJson
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_main.model.entity.MainAppConfigEntity
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
    private var _appConfig = MutableStateFlow<MainAppConfigEntity?>(null)
    val appConfig: StateFlow<MainAppConfigEntity?> get() = _appConfig

    init {
        viewModelScope.launch {
            _appConfig.value = appContext.appConfigDataStore.asyncDecode(DataStoreAgent.APP_CONFIG).toTypeEntity<MainAppConfigEntity>() ?: MainAppConfigEntity(true)
        }
    }

    fun saveAppConfig() { viewModelScope.launch { appContext.appConfigDataStore.asyncEncode(DataStoreAgent.APP_CONFIG, toJson(MainAppConfigEntity())) } }

    suspend fun getReadedAppConfig(): MainAppConfigEntity? {
        return suspendCancellableCoroutine<MainAppConfigEntity?> { continuation ->
            viewModelScope.launch {
                runCatching { continuation.resume(appContext.appConfigDataStore.asyncDecode(DataStoreAgent.APP_CONFIG).toTypeEntity<MainAppConfigEntity>()) }.onFailure { continuation.resume(null) }
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