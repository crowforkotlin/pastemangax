package com.crow.module_bookshelf.network

import com.crow.base.current_project.BaseResultResp
import com.crow.base.current_project.BaseStrings
import com.crow.module_bookshelf.model.resp.BookshelfResp
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Query

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_bookshelf/src/main/kotlin/com/crow/module_bookshelf/network
 * @Time: 2023/3/22 23:53
 * @Author: CrowForKotlin
 * @Description: BookShelfService
 * @formatter:on
 **************************/
interface BookShelfService {

    @GET(BaseStrings.URL.BookShelf)
    fun getBookShlef(@Query("offset") start: Int, @Query("limit") limit: Int, @Query("ordering") order: String) : Flow<BaseResultResp<BookshelfResp>>
}