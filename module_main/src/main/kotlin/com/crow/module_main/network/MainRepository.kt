package com.crow.module_main.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_main/src/main/kotlin/com/crow/module_main/network
 * @Time: 2023/3/13 23:36
 * @Author: CrowForKotlin
 * @Description: ContainerRepository
 * @formatter:on
 **************************/
class MainRepository(private val service: MainService) {

    fun getComicHistory(offset: Int, limit: Int, order: String) =
        service.getComicHistory(offset = offset, limit = limit, order = order).flowOn(Dispatchers.IO)


    fun getNovelHistory(offset: Int, limit: Int, order: String) =
        service.getNovelHistory(offset = offset, limit = limit, order = order).flowOn(Dispatchers.IO)
}