package com.crow.module_home.model.intent

import com.crow.mangax.copymanga.resp.BaseResultResp
import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_home.model.resp.homepage.ComicDatas
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.crow.module_home.model.resp.homepage.results.Results
import com.crow.module_home.model.resp.search.SearchComicResp
import com.crow.module_home.model.resp.search.SearchNovelResp
import com.crow.module_home.model.resp.topic.TopicResp

open class HomeIntent : BaseMviIntent() {

    data class GetHomePage(val homePageData: BaseResultResp<Results>? = null) : HomeIntent()

    data class GetRecPageByRefresh(val recPageData: BaseResultResp<ComicDatas<RecComicsResult>>? = null) : HomeIntent()

    data class SearchComic(val keyword: String, val type: String, val searchComicResp: SearchComicResp? = null) : HomeIntent()

    data class SearchNovel(val keyword: String, val type: String, val searchNovelResp: SearchNovelResp? = null) : HomeIntent()

    data class GetTopic(val pathword: String, val topicResp: TopicResp? = null) : HomeIntent()

}