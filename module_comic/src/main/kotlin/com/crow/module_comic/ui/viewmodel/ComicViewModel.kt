package com.crow.module_comic.ui.viewmodel

import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_comic.model.intent.ComicIntent
import com.crow.module_comic.model.resp.ChapterResultsResp
import com.crow.module_comic.model.resp.InfoResultsResp
import com.crow.module_comic.network.ComicRepository

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/viewmodel
 * @Time: 2023/3/15 0:20
 * @Author: CrowForKotlin
 * @Description: ComicViewModel
 * @formatter:on
 **************************/
class ComicViewModel(val repository: ComicRepository) : BaseMviViewModel<ComicIntent>() {

    private var mChapterStartIndex = 0

    var mComicInfoPage: InfoResultsResp? = null
        private set

    var mComicChapterPage: ChapterResultsResp? = null
        private set

    var mIsSaveData = false
        private set

    override fun dispatcher(intent: ComicIntent) {
        when (intent) {
            is ComicIntent.GetComicInfo -> getComicInfo(intent)
            is ComicIntent.GetComicChapter -> getComicChapter(intent)
            is ComicIntent.GetComic -> getComic(intent)
            is ComicIntent.GetComicBrowserHistory -> getComicBrowserHistory(intent)
        }
    }

    private fun getComicBrowserHistory(intent: ComicIntent.GetComicBrowserHistory) {
        flowResult(intent, repository.getComicBrowserHistory(intent.pathword)) { value ->
            intent.copy(browserHistory = value.mResults)
        }
    }

    private fun getComicInfo(intent: ComicIntent.GetComicInfo) {
        flowResult(intent, repository.getComicInfo(intent.pathword)) { value ->
            mComicInfoPage = value.mResults
            intent.copy(comicInfo = value.mResults)
        }
    }

    private fun getComicChapter(intent: ComicIntent.GetComicChapter) {
        flowResult(intent, repository.getComicChapter(intent.pathword, mChapterStartIndex, 100)) { value ->
            mComicChapterPage = value.mResults
            intent.copy(comicChapter = value.mResults)
        }
    }

    private fun getComic(intent: ComicIntent.GetComic) {
        flowResult(intent, repository.getComic(intent.pathword, intent.uuid)) { value ->
            intent.copy(comicChapter = value.mResults)
        }
    }

    fun clearAllData() {
        mComicInfoPage = null
        mComicChapterPage = null
    }

    fun setSaveData(isSaveData: Boolean) {
        mIsSaveData = isSaveData
    }
}