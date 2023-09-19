package com.crow.module_bookshelf.model.resp.bookshelf_comic

import com.crow.module_bookshelf.model.resp.bookshelf.Author
import com.squareup.moshi.Json

data class Comic(
    @Json(name =  "author")
    val mAuthor: List<Author>,

    @Json(name =  "b_display")
    val mBDisplay: Boolean,

    @Json(name =  "browse")
    val mBrowse: Browse?,

    @Json(name =  "cover")
    val mCover: String,

    @Json(name =  "datetime_updated")
    val mDatetimeUpdated: String,

    @Json(name =  "females")
    val mFemales: List<Any>,

    @Json(name =  "last_chapter_id")
    val mLastChapterId: String,

    @Json(name =  "last_chapter_name")
    val mLastChapterName: String,

    @Json(name =  "males")
    val mMales: List<Any>,

    @Json(name =  "name")
    val mName: String,

    @Json(name =  "path_word")
    val mPathWord: String,

    @Json(name =  "popular")
    val mPopular: Int,

    @Json(name =  "status")
    val mStatus: Int,

    @Json(name =  "theme")
    val mTheme: List<Any>,

    @Json(name =  "uuid")
    val mUuid: String
)