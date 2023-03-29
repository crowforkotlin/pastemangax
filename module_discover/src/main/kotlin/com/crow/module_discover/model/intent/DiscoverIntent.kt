package com.crow.module_discover.model.intent

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_discover.model.resp.DiscoverHomeResp
import com.crow.module_discover.model.resp.DiscoverTagResp

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_discovery/src/main/kotlin/com/crow/module_discovery/ui/model/intent
 * @Time: 2023/3/28 23:18
 * @Author: CrowForKotlin
 * @Description: DiscoverIntent
 * @formatter:on
 **************************/
open class DiscoverIntent private constructor(): BaseMviIntent() {

    data class GetTag(val tagResp: DiscoverTagResp? = null) : DiscoverIntent()

    data class GetHome(val homeResp: DiscoverHomeResp? = null) : DiscoverIntent()
}