package com.crow.module_anime.model

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_anime.model.resp.discover.DiscoverPageResp

open class AnimeIntent : BaseMviIntent() {

    data class DiscoverPageIntent(val pages: DiscoverPageResp? = null) : AnimeIntent()

}