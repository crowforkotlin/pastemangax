package com.crow.module_main.ui.viewmodel

import android.text.Html
import com.crow.base.tools.extensions.logMsg
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_main.model.intent.ContainerIntent
import com.crow.module_main.network.ContainerRepository
import java.util.regex.Matcher
import java.util.regex.Pattern

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_main/src/main/kotlin/com/crow/module_main/ui
 * @Time: 2023/3/7 23:56
 * @Author: CrowForKotlin
 * @Description: ContainerViewModel
 * @formatter:on
 **************************/
class ContainerViewModel(val repository: ContainerRepository) : BaseMviViewModel<ContainerIntent>() {

    private fun getUpdateInfo(intent: ContainerIntent.GetUpdateInfo) {
        flowResult(intent, repository.getUpdateInfo()) { value -> intent.copy(appUpdateResp = value) }
    }

    private fun getQQGropu(intent: ContainerIntent.GetQQGroup) {
        flowResult(intent, repository.getQQGroup()) { value -> intent.copy(link = value.string()) }
    }

    override fun dispatcher(intent: ContainerIntent) {
        when(intent) {
            is ContainerIntent.GetUpdateInfo -> getUpdateInfo(intent)
            is ContainerIntent.GetQQGroup -> getQQGropu(intent)
        }
    }
}