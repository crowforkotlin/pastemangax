package com.crow.module_bookshelf.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_bookshelf/src/main/kotlin/com/crow/module_bookshelf/network
 * @Time: 2023/3/22 23:53
 * @Author: CrowForKotlin
 * @Description: BookShelfRepository
 * @formatter:on
 **************************/
class BookShelfRepository(private val service: BookShelfService) {

    fun getBookShelf(start: Int, limit: Int, order: String) = service.getBookShlef(start, limit, order).flowOn(Dispatchers.IO)

}