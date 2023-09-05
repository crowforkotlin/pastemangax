package com.crow.module_bookshelf.model.resp.bookshelf_comic


import com.crow.module_bookshelf.model.resp.bookshelf.LastBrowse
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookshelfComicResults(

    @SerialName(value = "b_folder")
    val mBFolder: Boolean,

    @SerialName(value = "comic")
    val mComic: Comic,

    @SerialName(value = "folder_id")
    val mFolderId: @Polymorphic Any?,

    @SerialName(value = "last_browse")
    val mLastBrowse: LastBrowse?,

    @SerialName(value = "name")
    val mName: @Polymorphic Any?,

    @SerialName(value = "uuid")
    val mUuid: Int
)