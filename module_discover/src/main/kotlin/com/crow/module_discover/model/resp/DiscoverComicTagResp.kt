package com.crow.module_discover.model.resp


import com.crow.module_discover.model.resp.comic_tag.Ordering
import com.crow.module_discover.model.resp.comic_tag.Theme
import com.crow.module_discover.model.resp.comic_tag.Top
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DiscoverComicTagResp(
    @Json(name = "ordering")
    val ordering: List<Ordering>,
    @Json(name = "theme")
    val theme: List<Theme>,
    @Json(name = "top")
    val top: List<Top>
)