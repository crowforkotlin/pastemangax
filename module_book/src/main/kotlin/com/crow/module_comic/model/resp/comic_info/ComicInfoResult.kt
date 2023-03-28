package com.crow.module_comic.model.resp.comic_info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ComicInfoResult(

    @Json(name = "alias")
    val mAlias: String?,

    @Json(name = "author")
    val mAuthor: List<Author>,

    @Json(name = "b_404")
    val mB404: Boolean,

    @Json(name = "b_hidden")
    val mBHidden: Boolean,

    @Json(name = "brief")
    val mBrief: String,

    @Json(name = "close_comment")
    val mBloseComment: Boolean,

    @Json(name = "close_roast")
    val mCloseRoast: Boolean,

    @Json(name = "clubs")
    val mClubs: List<Any>,

    @Json(name = "cover")
    val mCover: String,

    @Json(name = "datetime_updated")
    val mDatetimeUpdated: String,

    @Json(name = "females")
    val mFemales: List<Any>,

    @Json(name = "free_type")
    val mFreeType: FreeType,

    @Json(name = "img_type")
    val mImgType: Int,

    @Json(name = "last_chapter")
    val mLastChapter: LastChapter,

    @Json(name = "males")
    val mMales: List<Any>,

    @Json(name = "name")
    val mName: String,

    @Json(name = "parodies")
    val mParodies: List<Any>,

    @Json(name = "path_word")
    val mPathWord: String,

    @Json(name = "popular")
    val mPopular: Int,

    @Json(name = "reclass")
    val mReclass: Reclass,

    @Json(name = "region")
    val mRegion: Region,

    @Json(name = "restrict")
    val mRestrict: Restrict,

    @Json(name = "seo_baidu")
    val mSeoBaidu: String,

    @Json(name = "status")
    val mStatus: Status,

    @Json(name = "theme")
    val mTheme: List<Theme>,

    @Json(name = "uuid")
    val mUuid: String,
)