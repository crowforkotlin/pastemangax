package com.crow.module_discover.network

import com.crow.base.current_project.BaseResultResp
import com.crow.base.current_project.BaseStrings
import com.crow.module_discover.model.resp.DiscoverComicHomeResp
import com.crow.module_discover.model.resp.DiscoverComicTagResp
import com.crow.module_discover.model.resp.DiscoverNovelHomeResp
import com.crow.module_discover.model.resp.DiscoverNovelTagResp
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Query

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_discover/src/main/kotlin/com/crow/module_discover/network
 * @Time: 2023/3/29 11:29
 * @Author: CrowForKotlin
 * @Description: DiscoverServuce
 * @formatter:on
 **************************/
interface DiscoverService {

    @GET(BaseStrings.URL.DiscoverComicTag)
    fun getComicTag(@Query("type") type: Int = 1): Flow<BaseResultResp<DiscoverComicTagResp>>

    @GET(BaseStrings.URL.DiscoverComicHome)
    fun getComicHome(
        @Query("offset") start: Int,
        @Query("limit") limit: Int,
        @Query("ordering") order: String = "-datetime_updated",
        @Query("theme") theme: String = "",
    ): Flow<BaseResultResp<DiscoverComicHomeResp>>

    @GET(BaseStrings.URL.DiscoverNovelTag)
    fun getNovelTag(@Query("type") type: Int = 1): Flow<BaseResultResp<DiscoverNovelTagResp>>

    @GET(BaseStrings.URL.DiscoverNovelHome)
    fun getNovelHome(
        @Query("offset") start: Int,
        @Query("limit") limit: Int,
        @Query("ordering") order: String = "-datetime_updated",
        @Query("theme") theme: String = "",
    ): Flow<BaseResultResp<DiscoverNovelHomeResp>>
}