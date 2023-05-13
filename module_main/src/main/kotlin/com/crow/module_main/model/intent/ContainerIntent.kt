package com.crow.module_main.model.intent

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_main.model.resp.MainAppUpdateResp
import com.crow.module_main.model.resp.MainSiteResp

open class ContainerIntent : BaseMviIntent() {

    // 获取更新信息
    data class GetUpdateInfo(val appUpdateResp: MainAppUpdateResp? = null) : ContainerIntent()

    // 获取QQ群
    data class GetQQGroup(val link: String? = null) : ContainerIntent()

    // 获取站点
    data class GetDynamicSite(val siteResp: MainSiteResp? = null) : ContainerIntent()
}