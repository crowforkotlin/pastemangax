package com.crow.module_discover.model.resp

import com.crow.module_discover.model.resp.comic_home.DiscoverComicHomeResult
import com.squareup.moshi.Json


data class DiscoverComicHomeResp(

    @Json(name =  "limit")
    val mLimit: Int,

    @Json(name =  "list")
    val mList: List<DiscoverComicHomeResult>,

    @Json(name =  "offset")
    val mOffset: Int,

    @Json(name =  "total")
    val mTotal: Int
)