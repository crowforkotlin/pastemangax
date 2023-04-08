package com.crow.module_discover.model.intent

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_discover.model.resp.DiscoverComicHomeResp
import com.crow.module_discover.model.resp.DiscoverComicTagResp
import com.crow.module_discover.model.resp.DiscoverNovelHomeResp
import com.crow.module_discover.model.resp.DiscoverNovelTagResp

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_discovery/src/main/kotlin/com/crow/module_discovery/ui/model/intent
 * @Time: 2023/3/28 23:18
 * @Author: CrowForKotlin
 * @Description: DiscoverIntent
 * @formatter:on
 **************************/
open class DiscoverIntent private constructor(): BaseMviIntent() {

    data class GetComicTag(val comicTagResp: DiscoverComicTagResp? = null) : DiscoverIntent()

    data class GetComicHome(val comicHomeResp: DiscoverComicHomeResp? = null) : DiscoverIntent()

    data class GetNovelTag(val novelTagResp: DiscoverNovelTagResp? = null) : DiscoverIntent()

    data class GetNovelHome(val novelHomeResp: DiscoverNovelHomeResp? = null) : DiscoverIntent()
}