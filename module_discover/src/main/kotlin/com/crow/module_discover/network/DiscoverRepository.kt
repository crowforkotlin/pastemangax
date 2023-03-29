package com.crow.module_discover.network

import com.crow.base.current_project.BaseResultResp
import com.crow.module_discover.model.resp.DiscoverTagResp
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_discover/src/main/kotlin/com/crow/module_discover/network
 * @Time: 2023/3/29 11:28
 * @Author: CrowForKotlin
 * @Description: DiscoveryRepository
 * @formatter:on
 **************************/
class DiscoverRepository(val service: DiscoverService) {

    fun getTag() = service.getTag()

    fun getHome(start: Int, limit: Int, order: String, theme: String = "") = service.getHome(start, limit, order, theme)

}