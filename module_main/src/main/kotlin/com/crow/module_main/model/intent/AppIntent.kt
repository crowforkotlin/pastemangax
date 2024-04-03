package com.crow.module_main.model.intent

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_main.model.resp.MainAppUpdateHistoryResp
import com.crow.module_main.model.resp.MainAppUpdateInfoResp
import com.crow.module_main.model.resp.MainNoticeResp
import com.crow.module_main.model.resp.MainSiteResp

open class AppIntent : BaseMviIntent() {

    // 获取更新历史记录
    data class GetUpdateHistory(val appUpdateResp: MainAppUpdateHistoryResp? = null) : AppIntent()

    // 获取更新信息
    data class GetUpdateInfo(val appUpdateResp: MainAppUpdateInfoResp? = null) : AppIntent()

    // 获取群信息
    data class GetQQGroup(val link: String? = null) : AppIntent()

    // 获取站点
    data class GetDynamicSite(val siteResp: MainSiteResp? = null) : AppIntent()

    // 获取通知
    data class GetNotice(val force: Boolean, val notice: MainNoticeResp? = null) : AppIntent()
}