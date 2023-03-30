package com.crow.module_discover.network

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_discover/src/main/kotlin/com/crow/module_discover/network
 * @Time: 2023/3/29 11:28
 * @Author: CrowForKotlin
 * @Description: DiscoveryRepository
 * @formatter:on
 **************************/
class DiscoverRepository(val service: DiscoverService) {

    fun getComicTag() = service.getComicTag()

    fun getComicHome(start: Int, limit: Int, order: String, theme: String = "") = service.getComicHome(start, limit, order, theme)

    fun getNovelTag() = service.getNovelTag()

    fun getNovelHome(start: Int, limit: Int, order: String, theme: String = "") = service.getNovelHome(start, limit, order, theme)
}