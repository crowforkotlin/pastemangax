package com.crow.module_discover.network

import com.crow.base.current_project.BaseResultResp
import com.crow.base.current_project.BaseStrings
import com.crow.module_discover.model.resp.DiscoverHomeResp
import com.crow.module_discover.model.resp.DiscoverTagResp
import com.crow.module_discover.model.resp.home.DiscoverHomeResult
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

    @GET(BaseStrings.URL.DiscoverTag)
    fun getTag(@Query("type") type: Int = 1): Flow<BaseResultResp<DiscoverTagResp>>

    @GET(BaseStrings.URL.DiscoverHome)
    fun getHome(
        @Query("offset") start: Int,
        @Query("limit") limit: Int,
        @Query("ordering") order: String = "-datetime_updated",
        @Query("theme") theme: String = "",
    ): Flow<BaseResultResp<DiscoverHomeResp>>
}