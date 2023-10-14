package com.crow.module_anime.model

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_anime.model.resp.chapter.AnimeChapterResp
import com.crow.module_anime.model.resp.discover.DiscoverPageResp
import com.crow.module_anime.model.resp.info.AnimeInfoResp

open class AnimeIntent : BaseMviIntent() {

    data class ChapterListIntent(val pathword: String, val chapters: AnimeChapterResp? = null) : AnimeIntent()

    data class DiscoverPageIntent(val pages: DiscoverPageResp? = null) : AnimeIntent()

    data class PageInfoIntent(val pathword: String, val info: AnimeInfoResp? = null) : AnimeIntent()


}