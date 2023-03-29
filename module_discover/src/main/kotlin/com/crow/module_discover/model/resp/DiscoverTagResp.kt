package com.crow.module_discover.model.resp


import com.crow.module_discover.model.resp.tag.Ordering
import com.crow.module_discover.model.resp.tag.Theme
import com.crow.module_discover.model.resp.tag.Top
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DiscoverTagResp(
    @Json(name = "ordering")
    val ordering: List<Ordering>,
    @Json(name = "theme")
    val theme: List<Theme>,
    @Json(name = "top")
    val top: List<Top>
)