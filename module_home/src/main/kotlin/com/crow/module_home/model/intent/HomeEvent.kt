package com.crow.module_home.model.intent

import com.crow.base.viewmodel.mvi.BaseMviEvent
import com.crow.module_home.model.resp.ComicResultResp
import com.crow.module_home.model.resp.homepage.ComicDatas
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.crow.module_home.model.resp.homepage.results.Results

sealed class HomeEvent : BaseMviEvent() {

    data class GetHomePage(val homePageData: ComicResultResp<Results>? = null) : HomeEvent()

    data class GetRecPageByRefresh(val recPageData: ComicResultResp<ComicDatas<RecComicsResult>>? = null) : HomeEvent()

}