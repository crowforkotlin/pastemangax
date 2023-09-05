package com.crow.module_book.model.resp.novel_info

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NovelInfoResult(
    @SerialName(value = "author")
    val mAuthor: List<Author>,

    @SerialName(value = "brief")
    val mBrief: String,

    @SerialName(value = "close_comment")
    val mCloseComment: Boolean,

    @SerialName(value = "close_roast")
    val mCloseRoast: Boolean,

    @SerialName(value = "cover")
    val mCover: String,

    @SerialName(value = "datetime_updated")
    val mDatetimeUpdated: String,

    @SerialName(value = "last_chapter")
    val mLastChapter: LastChapter,

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "parodies")
    val mParodies: List<@Polymorphic Any>,

    @SerialName(value = "path_word")
    val mPathWord: String,

    @SerialName(value = "popular")
    val mPopular: Int,

    @SerialName(value = "region")
    val mRegion: Region,

    @SerialName(value = "status")
    val mStatus: Status,

    @SerialName(value = "theme")
    val mTheme: List<Theme>,

    @SerialName(value = "uuid")
    val mUuid: String
)