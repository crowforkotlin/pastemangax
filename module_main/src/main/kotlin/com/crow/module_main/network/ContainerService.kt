package com.crow.module_main.network

import com.crow.base.current_project.BaseStrings
import com.crow.module_main.model.resp.MainAppUpdateResp
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.http.GET

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_main/src/main/kotlin/com/crow/module_main/network
 * @Time: 2023/3/13 23:39
 * @Author: CrowForKotlin
 * @Description: ContainerService
 * @formatter:on
 **************************/
interface ContainerService {

    @GET(BaseStrings.URL.UpdateInfo)
    fun getUpdateInfo(): Flow<MainAppUpdateResp>

    @GET(BaseStrings.URL.QQGroup)
    fun getQQGroup(): Flow<ResponseBody>
}