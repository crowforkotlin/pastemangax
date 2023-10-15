package com.crow.module_anime.model

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_anime.model.req.RegReq
import com.crow.module_anime.model.resp.UserFailureResp
import com.crow.module_anime.model.resp.chapter.AnimeChapterResp
import com.crow.module_anime.model.resp.discover.DiscoverPageResp
import com.crow.module_anime.model.resp.info.AnimeInfoResp
import com.crow.module_anime.model.resp.login.UserLoginResp
import com.crow.module_anime.model.resp.reg.UserRegResp
import com.crow.module_anime.model.resp.video.AnimeVideoResp

open class AnimeIntent : BaseMviIntent() {

    data class ChapterListIntent(val pathword: String, val chapters: AnimeChapterResp? = null) : AnimeIntent()

    data class DiscoverPageIntent(val pages: DiscoverPageResp? = null) : AnimeIntent()

    data class PageInfoIntent(val pathword: String, val info: AnimeInfoResp? = null) : AnimeIntent()

    data class RegIntent(
        val reg: RegReq,
        val failureResp: UserFailureResp? = null,
        val user: UserRegResp? = null
    ) : AnimeIntent()

    data class LoginIntent(
        val username: String,
        val password: String,
        val failureResp: UserFailureResp? = null,
        val user: UserLoginResp? = null
    ) : AnimeIntent()

    data class AnimeVideoIntent(
        val pathword: String,
        val chapterUUID: String,
        val video: AnimeVideoResp? = null
    ) : AnimeIntent()
}