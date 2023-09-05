package com.crow.module_home.model.resp.homepage

import com.crow.module_home.model.resp.homepage.results.AuthorResult
import com.crow.module_home.model.resp.homepage.results.FreeTypeResult
import com.crow.module_home.model.resp.homepage.results.ThemeResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

@Serializable
data class FinishComic(

    @SerialName(value = "name")
    val mName: String,

    @SerialName(value = "path_word")
    val mPathWord: String,

    @SerialName(value = "free_type")
    val mFreeType: FreeTypeResult,

    @SerialName(value = "author")
    val mAuthorResult: List<AuthorResult>,

    @SerialName(value = "theme")
    val mTheme: List<ThemeResult>,

    @SerialName(value = "cover")
    val mImageUrl: String,

    @SerialName(value = "popular")
    val mPopular: Int,

    @SerialName(value = "datetime_updated")
    val mDatetimeUpdated: String,
)