package com.crow.module_home.model.resp.homepage.results

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


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

@Serializable
data class ComicResultX(

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String,

    @SerialName(value = "author")
    val mAuthorResult: List<AuthorResult>,

    @SerialName(value = "img_type")
    val mImageType: Int,

    @SerialName(value = "theme")
    val mTheme: List<ThemeResult>,

    @SerialName(value = "cover")
    val mImageUrl: String,

    @SerialName(value = "popular")
    val mPopular: Int,

    @SerialName(value = "datetime_updated")
    val mDatetimeUpdated: String,

    @SerialName(value = "last_chapter_name")
    val mLastChapterName: String,
)