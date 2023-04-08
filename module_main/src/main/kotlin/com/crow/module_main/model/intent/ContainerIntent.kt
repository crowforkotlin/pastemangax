package com.crow.module_main.model.intent

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_main.model.resp.MainAppUpdateResp

open class ContainerIntent : BaseMviIntent() {
    data class GetUpdateInfo(val appUpdateResp: MainAppUpdateResp? = null) : ContainerIntent()

    data class GetQQGroup(val link: String? = null) : ContainerIntent()
}