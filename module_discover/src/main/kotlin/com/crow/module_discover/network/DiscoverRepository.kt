package com.crow.module_discover.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_discover/src/main/kotlin/com/crow/module_discover/network
 * @Time: 2023/3/29 11:28
 * @Author: CrowForKotlin
 * @Description: DiscoveryRepository
 * @formatter:on
 **************************/
class DiscoverRepository(val service: DiscoverService) {

    fun getComicTag() = service.getComicTag().flowOn(Dispatchers.IO)

    fun getComicHome(
        start: Int,
        limit: Int,
        order: String,
        theme: String,
        region: String,
    ) = service.getComicHome(start, limit, order = order, theme = theme, region = region).flowOn(
        Dispatchers.IO)

    fun getNovelTag() = service.getNovelTag().flowOn(Dispatchers.IO)

    fun getNovelHome(start: Int, limit: Int, order: String, theme: String = "") =
        service.getNovelHome(start, limit, order, theme).flowOn(Dispatchers.IO)
}