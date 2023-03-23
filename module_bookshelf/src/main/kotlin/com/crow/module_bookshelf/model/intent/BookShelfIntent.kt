package com.crow.module_bookshelf.model.intent

import com.crow.base.ui.viewmodel.mvi.BaseMviIntent
import com.crow.module_bookshelf.model.resp.BookshelfResp

open class BookShelfIntent : BaseMviIntent() {

    data class GetBookShelf(val bookshelfResp: BookshelfResp? = null) : BookShelfIntent()
}
