package com.crow.module_main.network

import com.crow.mangax.copymanga.BaseStrings
import com.crow.module_main.model.resp.MainAppUpdateResp
import com.crow.module_main.model.resp.MainSiteResp
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_main/src/main/kotlin/com/crow/module_main/network
 * @Time: 2023/3/13 23:39
 * @Author: CrowForKotlin
 * @Description: ContainerService
 * @formatter:on
 **************************/
interface AppService {

    @GET
    fun getUpdateInfo(@Url url: String = BaseStrings.URL.Crow_UpdateInfo): Flow<MainAppUpdateResp>

    @GET
    fun getQQGroup(@Url url: String = BaseStrings.URL.Crow_QQGroup): Flow<ResponseBody>

    @GET
    fun getSite(@Url url: String = BaseStrings.URL.Crow_Site): Flow<MainSiteResp>
}