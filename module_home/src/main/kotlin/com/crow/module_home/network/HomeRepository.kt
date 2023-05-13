package com.crow.module_home.network

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/model
 * @Time: 2023/3/6 9:12
 * @Author: CrowForKotlin
 * @Description: HomeRepository
 * @formatter:on
 **************************/
class HomeRepository(private val service: HomeService) {

    fun getHomePage() = service.getHomePage()

    fun getRecPageByRefresh(limit: Int, start: Int) = service.getRecPage(start, limit)

    fun doSearchComic(keyword: String, type: String, start: Int, limit: Int) = service.doSearchComic(keyword, type, start, limit)

    fun doSearchNovel(keyword: String, type: String, start: Int, limit: Int) = service.doSearchNovel(keyword, type, start, limit)
}