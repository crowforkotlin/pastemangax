package com.crow.module_bookshelf.model.resp.bookshelf_comic


import com.crow.module_bookshelf.model.resp.bookshelf.LastBrowse
import com.squareup.moshi.Json


data class BookshelfComicResults(

    @Json(name =  "b_folder")
    val mBFolder: Boolean,

    @Json(name =  "comic")
    val mComic: Comic,

    @Json(name =  "folder_id")
    val mFolderId: Any?,

    @Json(name =  "last_browse")
    val mLastBrowse: LastBrowse?,

    @Json(name =  "name")
    val mName: Any?,

    @Json(name =  "uuid")
    val mUuid: Int
)