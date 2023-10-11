package com.crow.module_anime.model.resp.info


import com.squareup.moshi.Json

data class Cartoon(
    @Json(name = "b_subtitle")
    val mBSubtitle: Boolean,

    @Json(name = "brief")
    val mBrief: String,

    @Json(name = "cartoon_type")
    val mCartoonType: CartoonType,

    @Json(name = "category")
    val mCategory: Category,

    @Json(name = "company")
    val mCompany: Company,

    @Json(name = "cover")
    val mCover: String,

    @Json(name = "datetime_updated")
    val mDatetimeUpdated: String?,

    @Json(name = "females")
    val mFemales: List<Any>,

    @Json(name = "free_type")
    val mFreeType: FreeType,

    @Json(name = "grade")
    val mGrade: Grade,

    @Json(name = "last_chapter")
    val mLastChapter: LastChapter,

    @Json(name = "males")
    val mMales: List<Any>,

    @Json(name = "name")
    val mName: String,

    @Json(name = "parodies")
    val mParodies: Any?,

    @Json(name = "path_word")
    val mPathWord: String,

    @Json(name = "popular")
    val mPopular: Int,

    @Json(name = "theme")
    val mTheme: List<Theme>,

    @Json(name = "uuid")
    val mUUID: String,

    @Json(name = "years")
    val mYears: String
)