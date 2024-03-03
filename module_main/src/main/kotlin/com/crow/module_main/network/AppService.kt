package com.crow.module_main.network

import com.crow.mangax.copymanga.BaseStrings
import com.crow.module_main.model.resp.MainAppUpdateHistoryResp
import com.crow.module_main.model.resp.MainAppUpdateInfoResp
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
    fun getUpdateInfo(@Url url: String = BaseStrings.Repository.UpdateInfo): Flow<MainAppUpdateInfoResp>

    @GET
    fun getUpdateHistory(@Url url: String = BaseStrings.Repository.UpdateHistory): Flow<MainAppUpdateHistoryResp>

    @GET
    fun getGroup(@Url url: String = BaseStrings.Repository.GROUP): Flow<ResponseBody>

    @GET
    fun getSite(@Url url: String = BaseStrings.Repository.SITE): Flow<MainSiteResp>
}