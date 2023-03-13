package com.crow.module_home.network

import com.crow.module_home.model.resp.ComicResultResp
import com.crow.module_home.model.resp.homepage.ComicDatas
import com.crow.module_home.model.resp.homepage.results.RecComicsResult
import com.crow.module_home.model.resp.homepage.results.Results
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Query

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/network
 * @Time: 2023/3/6 9:13
 * @Author: CrowForKotlin
 * @Description: HomeService
 * @formatter:on
 **************************/
interface HomeService {

    @GET("/api/v3/h5/homeIndex")
    fun getHomePage(): Flow<ComicResultResp<Results>>

    @GET("/api/v3/recs")
    fun getRecPage(@Query("limit") limit: Int, @Query("offset") start: Int, @Query("pos") pos:Int = 3200102): Flow<ComicResultResp<ComicDatas<RecComicsResult>>>
}