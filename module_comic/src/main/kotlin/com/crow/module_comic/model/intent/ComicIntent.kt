package com.crow.module_comic.model.intent

import com.crow.base.viewmodel.mvi.BaseMviIntent
import com.crow.module_comic.model.resp.ComicChapterResp
import com.crow.module_comic.model.resp.ComicInfoResp
import com.crow.module_comic.model.resp.ComicResp

sealed class ComicIntent : BaseMviIntent() {

    data class GetComicInfo(val pathword: String, val comicInfo: ComicInfoResp? = null) : ComicIntent()

    data class GetComicChapter(val pathword: String, val comicChapter: ComicChapterResp? = null) : ComicIntent()

    data class GetComic(val pathword: String, val uuid: String,  val comicChapter: ComicResp? = null) : ComicIntent()

}