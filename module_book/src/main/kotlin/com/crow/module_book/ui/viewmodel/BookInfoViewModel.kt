package com.crow.module_book.ui.viewmodel

import com.crow.base.app.appContext
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.ui.viewmodel.mvi.BaseMviViewModel
import com.crow.module_book.R
import com.crow.module_book.model.intent.BookIntent
import com.crow.module_book.model.resp.*
import com.crow.module_book.network.BookRepository
import java.net.HttpURLConnection

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/ui/viewmodel
 * @Time: 2023/3/15 0:20
 * @Author: CrowForKotlin
 * @Description: ComicViewModel
 * @formatter:on
 **************************/
class BookInfoViewModel(val repository: BookRepository) : BaseMviViewModel<BookIntent>() {

    private var mChapterStartIndex = 0

    var mComicInfoPage: ComicInfoResp? = null
        private set

    var mComicChapterPage: ComicChapterResp? = null
        private set

    var mNovelChapterPage: NovelChapterResp? = null
        private set

    var mNovelInfoPage: NovelInfoResp? = null
        private set

    var comicPage: ComicPageResp? = null
        private set

    override fun dispatcher(intent: BookIntent) {
        when (intent) {
            is BookIntent.GetComicInfo -> getComicInfo(intent)
            is BookIntent.GetComicChapter -> getComicChapter(intent)
            is BookIntent.GetComicPage -> getComicPage(intent)
            is BookIntent.GetComicBrowserHistory -> getComicBrowserHistory(intent)
            is BookIntent.GetNovelInfo -> getNovelInfo(intent)
            is BookIntent.GetNovelChapter -> getNovelChapter(intent)
            is BookIntent.GetNovelPage -> getNovelPage(intent)
            is BookIntent.GetNovelBrowserHistory -> getNovelBrowserHistory(intent)
            is BookIntent.GetNovelCatelogue -> getNovelCatelogue(intent)
        }
    }


    fun reCountPos(pos: Int) { mChapterStartIndex = pos * 100 }

    fun doNovelDatasIsNotNull(): Boolean = mNovelChapterPage != null && mNovelInfoPage != null
    fun doComicDatasIsNotNull(): Boolean = mComicChapterPage != null && mComicInfoPage != null



    private fun getComicBrowserHistory(intent: BookIntent.GetComicBrowserHistory) {
        flowResult(intent, repository.getComicBrowserHistory(intent.pathword)) { value -> intent.copy(comicBrowser = value.mResults) }
    }

    private fun getComicInfo(intent: BookIntent.GetComicInfo) {
        flowResult(intent, repository.getComicInfo(intent.pathword)) { value ->
            mComicInfoPage = value.mResults
            intent.copy(comicInfo = value.mResults)
        }
    }

    private fun getComicChapter(intent: BookIntent.GetComicChapter) {
        flowResult(intent, repository.getComicChapter(intent.pathword, mChapterStartIndex, 100)) { value ->
            if (value.mCode == HttpURLConnection.HTTP_OK) {
                val comicChapterPage = toTypeEntity<ComicChapterResp>(value.mResults)
                mComicChapterPage = comicChapterPage
                intent.copy(comicChapter = comicChapterPage)
            }
            else {
                intent.copy(invalidResp = appContext.getString(R.string.BookComicRequestThrottled, Regex("\\d+").find(value.mMessage)?.value ?: "0").toString())
            }
        }
    }

    private fun getComicPage(intent: BookIntent.GetComicPage) {
        flowResult(intent, repository.getComicPage(intent.pathword, intent.uuid)) { value ->
            comicPage = value.mResults
            intent.copy(comicPage = value.mResults)
        }
    }

    private fun getNovelInfo(intent: BookIntent.GetNovelInfo) {
        flowResult(intent, repository.getNovelInfo(intent.pathword)) { value ->
            mNovelInfoPage = value.mResults
            intent.copy(novelInfo = value.mResults)
        }
    }

    private fun getNovelChapter(intent: BookIntent.GetNovelChapter) {
        flowResult(intent, repository.getNovelChapter(intent.pathword)) { value ->
            if (value.mCode == HttpURLConnection.HTTP_OK) {
                val novelChapterResp = toTypeEntity<NovelChapterResp>(value.mResults)
                mNovelChapterPage = novelChapterResp
                intent.copy(novelChapter = novelChapterResp)
            } else {
                intent.copy(invalidResp = value.mMessage)
            }
        }
    }
    private fun getNovelBrowserHistory(intent: BookIntent.GetNovelBrowserHistory) {
        flowResult(intent, repository.getNovelBrowserHistory(intent.pathword)) { value ->
            intent.copy(novelBrowser = value.mResults)
        }
    }
    private fun getNovelPage(intent: BookIntent.GetNovelPage) {
        flowResult(intent, repository.getNovelPage(intent.pathword)) { value ->
            intent.copy(novelPage = value)
        }
    }

    private fun getNovelCatelogue(intent: BookIntent.GetNovelCatelogue) {
        flowResult(intent, repository.getNovelCatelogue(intent.pathword)) { value ->
            intent.copy(novelCatelogue = value.mResults)
        }
    }

    fun clearAllData() {
        mComicInfoPage = null
        mComicChapterPage = null
    }
}