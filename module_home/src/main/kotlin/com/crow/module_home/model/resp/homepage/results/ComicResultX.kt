package com.crow.module_home.model.resp.homepage.results

import com.squareup.moshi.Json


/**
 * Hot comic result
 *
 * @property mAuthorResult 作者集
 * @property mImageUrl 图片路径
 * @property mDatetimeUpdated 更新日期
 * @property mImageType 图片类型
 * @property mLastChapterName 当前（第几话）名称
 * @property mName 漫画名称
 * @property mPathWord 路径词
 * @property mPopular 热度（应该是浏览量）
 * @constructor Create empty Hot comic result
 */

data class ComicResultX(

    @Json(name =  "name")
    val mName: String,

    @Json(name =  "path_word")
    val mPathWord: String,

    @Json(name =  "author")
    val mAuthorResult: List<AuthorResult>,

    @Json(name =  "img_type")
    val mImageType: Int,

    @Json(name =  "theme")
    val mTheme: List<ThemeResult>,

    @Json(name =  "cover")
    val mImageUrl: String,

    @Json(name =  "popular")
    val mPopular: Int,

    @Json(name =  "datetime_updated")
    val mDatetimeUpdated: String,

    @Json(name =  "last_chapter_name")
    val mLastChapterName: String,
)