package com.crow.module_discover.model.resp


import com.crow.module_discover.model.resp.comic_home.DiscoverComicHomeResult
import com.crow.module_discover.model.resp.novel_home.DiscoverNovelHomeResult
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DiscoverNovelHomeResp(
    @Json(name = "limit")
    val mLimit: Int,

    @Json(name = "list")
    val mList: List<DiscoverNovelHomeResult>,

    @Json(name = "offset")
    val mOffset: Int,

    @Json(name = "total")
    val mTotal: Int
)