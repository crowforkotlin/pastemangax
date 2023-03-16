package com.crow.module_comic.model.resp


import com.crow.module_comic.model.resp.comic_info.Results
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Comic info resp
 *
 * @property mCode 消息码
 * @property mMessage 消息
 * @property mResults 结果集
 * @constructor Create empty Comic info resp
 */
@JsonClass(generateAdapter = true)
data class ComicInfoResp(

    @Json(name = "code")
    val mCode: Int,

    @Json(name = "message")
    val mMessage: String,

    @Json(name = "results")
    val mResults: Results,
)