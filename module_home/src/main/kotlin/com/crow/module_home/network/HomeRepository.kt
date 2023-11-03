package com.crow.module_home.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_home/src/main/kotlin/com/crow/module_home/model
 * @Time: 2023/3/6 9:12
 * @Author: CrowForKotlin
 * @Description: HomeRepository
 * @formatter:on
 **************************/
class HomeRepository(private val service: HomeService) {

    fun getHomePage() = service.getHomePage().flowOn(Dispatchers.IO)

    fun getRecPageByRefresh(limit: Int, start: Int) = service.getRecPage(start, limit).flowOn(Dispatchers.IO)

    fun getTopic(pathword: String, start: Int, limit: Int) = service.getTopic(pathword, start, limit).flowOn(Dispatchers.IO)

    fun doSearchComic(keyword: String, type: String, start: Int, limit: Int) = service.doSearchComic(keyword, type, start, limit).flowOn(Dispatchers.IO)

    fun doSearchNovel(keyword: String, type: String, start: Int, limit: Int) = service.doSearchNovel(keyword, type, start, limit).flowOn(Dispatchers.IO)
}