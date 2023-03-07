package com.crow.module_home.network

import com.crow.module_home.model.resp.HomePageResp
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/network
 * @Time: 2023/3/6 9:13
 * @Author: BarryAllen
 * @Description: HomeService
 * @formatter:on
 **************************/
interface HomeService {

    @GET("/api/v3/h5/homeIndex")
    fun getHomePage(): Flow<HomePageResp>
}