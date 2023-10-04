package com.crow.module_main.model.intent

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_main.model.resp.comic_history.ComicHistoryResp
import com.crow.module_main.model.resp.novel_history.NovelHistoryResp

open class MainIntent : BaseMviIntent() {

    // 获取历史记录
    data class GetComicHistory(val comic: ComicHistoryResp? = null) : MainIntent()

    data class GetNovelHistory(val novel: NovelHistoryResp? = null) : MainIntent()
}