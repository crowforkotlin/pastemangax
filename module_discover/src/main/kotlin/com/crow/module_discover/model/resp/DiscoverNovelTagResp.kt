package com.crow.module_discover.model.resp

import com.crow.module_discover.model.resp.novel_tag.Ordering
import com.crow.module_discover.model.resp.novel_tag.Theme
import com.crow.module_discover.model.resp.novel_tag.Top
import com.squareup.moshi.Json


data class DiscoverNovelTagResp(

    @Json(name =  "ordering")
    val mOrdering: List<Ordering>,

    @Json(name =  "theme")
    val mTheme: List<Theme>,

    @Json(name =  "top")
    val mTop: List<Top>
)