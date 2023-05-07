package com.crow.module_home.model.intent

import com.crow.base.copymanga.BaseResultResp
import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_home.model.resp.homepage.ComicDatas
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.crow.module_home.model.resp.homepage.results.Results

sealed class HomeIntent : BaseMviIntent() {

    data class GetHomePage(val homePageData: BaseResultResp<Results>? = null) : HomeIntent()

    data class GetRecPageByRefresh(val recPageData: BaseResultResp<ComicDatas<RecComicsResult>>? = null) : HomeIntent()

}