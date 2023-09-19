package com.crow.module_home.model.resp.search.comic_reuslt

import com.squareup.moshi.Json


/**
 * Result
 *
 * @property mAlias 漫画别名
 * @property mAuthor 作者集
 * @property mImageUrl 图片路径
 * @property mImgType 图片类型 0和1 暂时未知区别
 * @property mName 漫画名称
 * @property mPathWord 关键词
 * @property mPopular
 * @constructor Create empty Result
 */

data class SearchComicResult (

    @Json(name =  "alias")
    val mAlias: String?,

    @Json(name =  "author")
    val mAuthor: List<Author>,

    @Json(name =  "cover")
    val mImageUrl: String,

    @Json(name =  "img_type")
    val mImgType: Int?,

    @Json(name =  "name")
    val mName: String,

    @Json(name =  "path_word")
    val mPathWord: String,

    @Json(name =  "popular")
    val mPopular: Int
)