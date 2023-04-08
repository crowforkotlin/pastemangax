package com.crow.module_book.model.intent

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_book.model.resp.*
import okhttp3.ResponseBody

sealed class BookIntent : BaseMviIntent() {

    data class GetComicInfo(val pathword: String, val comicInfo: ComicInfoResp? = null) : BookIntent()

    data class GetComicChapter(val pathword: String, val comicChapter: ComicChapterResp? = null, val invalidResp: String? = null) : BookIntent()

    data class GetComicPage(val pathword: String, val uuid: String, val comicPage: ComicPageResp? = null) : BookIntent()

    data class GetComicBrowserHistory(val pathword: String, val comicBrowser: ComicBrowserResp? = null) : BookIntent()

    data class GetNovelInfo(val pathword: String, val novelInfo: NovelInfoResp? = null): BookIntent()

    data class GetNovelChapter(val pathword: String, val novelChapter: NovelChapterResp? = null, val invalidResp: String? = null): BookIntent()

    data class GetNovelBrowserHistory(val pathword: String, val novelBrowser: NovelBrowserResp? = null): BookIntent()

    data class GetNovelCatelogue(val pathword: String, val uuid: String, val novelCatelogue: NovelCatelogueResp? = null): BookIntent()

    data class GetNovelPage(val pathword: String, val uuid: String, val novelPage: ResponseBody? = null): BookIntent()

}