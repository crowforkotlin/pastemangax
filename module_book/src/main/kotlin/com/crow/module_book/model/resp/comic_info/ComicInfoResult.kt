package com.crow.module_book.model.resp.comic_info

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull

@Serializable
data class ComicInfoResult(

    @SerialName(value = "alias")
    val mAlias: String?,

    @SerialName(value = "author")
    val mAuthor: List<Author>,

    @SerialName(value = "b_404")
    val mB404: Boolean,

    @SerialName(value = "b_hidden")
    val mBHidden: Boolean,

    @SerialName(value = "brief")
    val mBrief: String,

    @SerialName(value = "close_comment")
    val mBloseComment: Boolean,

    @SerialName(value = "close_roast")
    val mCloseRoast: Boolean,

    @SerialName(value = "clubs")
    val mClubs: List<@Polymorphic Any>,

    @SerialName(value = "cover")
    val mCover: String,

    @SerialName(value = "datetime_updated")
    val mDatetimeUpdated: String,

    @SerialName(value = "females")
    val mFemales: List<JsonNull>,

    @SerialName(value = "free_type")
    val mFreeType: FreeType,

    @SerialName(value = "img_type")
    val mImgType: Int,

    @SerialName(value = "last_chapter")
    val mLastChapter: LastChapter,

    @SerialName(value = "males")
    val mMales: List<@Polymorphic Any>,

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "parodies")
    val mParodies: List<@Polymorphic Any>,

    @SerialName(value = "path_word")
    val mPathWord: String,

    @SerialName(value = "popular")
    val mPopular: Int,

    @SerialName(value = "reclass")
    val mReclass: Reclass,

    @SerialName(value = "region")
    val mRegion: Region,

    @SerialName(value = "restrict")
    val mRestrict: Restrict,

    @SerialName(value = "seo_baidu")
    val mSeoBaidu: String?,

    @SerialName(value = "status")
    val mStatus: Status,

    @SerialName(value = "theme")
    val mTheme: List<Theme>,

    @SerialName(value = "uuid")
    val mUuid: String,
)