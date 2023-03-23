package com.crow.module_comic.model.intent

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_comic.model.resp.ChapterResultsResp
import com.crow.module_comic.model.resp.ComicBrowserHistoryResp
import com.crow.module_comic.model.resp.ComicResultsResp
import com.crow.module_comic.model.resp.InfoResultsResp

sealed class ComicIntent : BaseMviIntent() {

    data class GetComicInfo(val pathword: String, val comicInfo: InfoResultsResp? = null) : ComicIntent()

    data class GetComicChapter(val pathword: String, val comicChapter: ChapterResultsResp? = null) : ComicIntent()

    data class GetComic(val pathword: String, val uuid: String,  val comicChapter: ComicResultsResp? = null) : ComicIntent()

    data class GetComicBrowserHistory(val pathword: String, val browserHistory: ComicBrowserHistoryResp? = null) : ComicIntent()
}