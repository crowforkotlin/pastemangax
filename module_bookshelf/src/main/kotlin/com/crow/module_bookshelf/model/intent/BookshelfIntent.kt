package com.crow.module_bookshelf.model.intent

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_bookshelf.model.resp.BookshelfComicfResp
import com.crow.module_bookshelf.model.resp.BookshelfNovelResp

open class BookshelfIntent : BaseMviIntent() {

    data class GetBookshelfComic(val bookshelfComicfResp: BookshelfComicfResp? = null) : BookshelfIntent()

    data class GetBookshelfNovel(val bookshelfNovelResp: BookshelfNovelResp? = null) : BookshelfIntent()
}
