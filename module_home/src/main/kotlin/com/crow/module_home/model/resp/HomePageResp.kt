package com.crow.module_home.model.resp

import com.crow.module_home.model.resp.homepage.results.Results
import com.squareup.moshi.Json


/**
 * Home page resp
 *
 * @property mCode 响应码
 * @property mMessage 消息
 * @property mResults 结果集
 * @constructor Create empty Home page resp
 */
data class HomePageResp(

    @Json(name = "code")
    val mCode: Int,

    @Json(name = "message")
    val mMessage: String,

    @Json(name = "results")
    val mResults: Results

)