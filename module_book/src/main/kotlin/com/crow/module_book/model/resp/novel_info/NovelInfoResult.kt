package com.crow.module_book.model.resp.novel_info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NovelInfoResult(
    @Json(name = "author")
    val mAuthor: List<Author>,

    @Json(name = "brief")
    val mBrief: String,

    @Json(name = "close_comment")
    val mCloseComment: Boolean,

    @Json(name = "close_roast")
    val mCloseRoast: Boolean,

    @Json(name = "cover")
    val mCover: String,

    @Json(name = "datetime_updated")
    val mDatetimeUpdated: String,

    @Json(name = "last_chapter")
    val mLastChapter: LastChapter,

    @Json(name = "name")
    val mName: String,

    @Json(name = "parodies")
    val mParodies: List<Any>,

    @Json(name = "path_word")
    val mPathWord: String,

    @Json(name = "popular")
    val mPopular: Int,

    @Json(name = "region")
    val mRegion: Region,

    @Json(name = "status")
    val mStatus: Status,

    @Json(name = "theme")
    val mTheme: List<Theme>,

    @Json(name = "uuid")
    val mUuid: String
)