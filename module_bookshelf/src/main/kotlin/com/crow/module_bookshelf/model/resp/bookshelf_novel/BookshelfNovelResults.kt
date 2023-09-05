package com.crow.module_bookshelf.model.resp.bookshelf_novel


import com.crow.module_bookshelf.model.resp.bookshelf.LastBrowse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookshelfNovelResults (

    @SerialName(value = "book")
    val mNovel: Novel,

    @SerialName(value = "last_browse")
    val mLastBrowse: LastBrowse?,

    @SerialName(value = "uuid")
    val mUuid: Int
)