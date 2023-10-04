package com.crow.module_home.model.resp.homepage

import com.crow.module_home.model.resp.homepage.results.AuthorResult
import com.crow.module_home.model.resp.homepage.results.FreeTypeResult
import com.crow.module_home.model.resp.homepage.results.ThemeResult
import com.squareup.moshi.Json


/**
 * Finish comic
 *
 * @property mName 漫画名称
 * @property mPathWord 路径词
 * @property mFreeType 免费 和 付费
 * @property mAuthorResult 作者
 * @property mTheme 主题
 * @property mImageUrl 图片路径
 * @property mPopular 热度
 * @property mDatetimeUpdated 更新日期
 * @constructor Create empty Finish comic
 */

data class FinishComic(

    @Json(name =  "name")
    val mName: String,

    @Json(name =  "path_word")
    val mPathWord: String,

    @Json(name =  "free_type")
    val mFreeType: FreeTypeResult,

    @Json(name =  "author")
    val mAuthorResult: List<AuthorResult>,

    @Json(name =  "theme")
    val mTheme: List<ThemeResult>,

    @Json(name =  "cover")
    val mImageUrl: String,

    @Json(name =  "popular")
    val mPopular: Int,

    @Json(name =  "datetime_updated")
    val mDatetimeUpdated: String?,
)