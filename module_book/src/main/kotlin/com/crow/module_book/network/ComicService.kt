package com.crow.module_book.network

import com.crow.mangax.copymanga.BaseStrings.PATH_WORD
import com.crow.mangax.copymanga.BaseStrings.URL
import com.crow.mangax.copymanga.resp.BaseNullableResultResp
import com.crow.mangax.copymanga.resp.BaseResultResp
import com.crow.module_book.model.resp.ComicBrowserResp
import com.crow.module_book.model.resp.ComicInfoResp
import com.crow.module_book.model.resp.ComicPageResp
import com.crow.module_book.model.resp.NovelBrowserResp
import com.crow.module_book.model.resp.NovelCatelogueResp
import com.crow.module_book.model.resp.NovelInfoResp
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_comic/src/main/kotlin/com/crow/module_comic/network
 * @Time: 2023/3/15 0:15
 * @Author: CrowForKotlin
 * @Description: ComicService
 * @formatter:on
 **************************/
interface ComicService {

    // 获取漫画信息
    @GET(URL.ComicInfo)
    fun getComicInfo(@Path(PATH_WORD) pathword: String): Flow<BaseResultResp<ComicInfoResp>>

    // 获取漫画章节列表
    @GET(URL.ComicChapter)
    fun getComicChapter(@Path(PATH_WORD) pathword: String, @Query("offset") start: Int, @Query("limit") limit: Int): Flow<BaseResultResp<Any>>

    // 获取漫画内容
    @GET(URL.ComicPage)
    fun getComicPage(@Path(PATH_WORD) pathword: String, @Path("uuid") uuid: String): Flow<BaseResultResp<ComicPageResp>>

    // 获取当前漫画已浏览的记录
    @GET(URL.ComicBrowserHistory)
    fun getComicBrowserHistory(@Path(PATH_WORD) pathword: String) : Flow<BaseResultResp<ComicBrowserResp>>

    // 获取小说信息
    @GET(URL.NovelInfo)
    fun getNovelInfo(@Path(PATH_WORD) pathword: String) : Flow<BaseResultResp<NovelInfoResp>>

    // 获取小说章节
    @GET(URL.NovelChapter)
    fun getNovelChapter(@Path(PATH_WORD) pathword: String) : Flow<BaseResultResp<Any>>

    // 获取小说内容 会得到一个txt文本
    @GET
    fun getNovelPage(@Url url: String) : Flow<ResponseBody>

    // 获取小说目录
    @GET(URL.NovelCatelogue)
    fun getNovelCatelogue(@Path(PATH_WORD) pathword: String) : Flow<BaseResultResp<NovelCatelogueResp>>

    // 获取当前小说已浏览记录
    @GET(URL.NovelBrowserHistory)
    fun getNovelBrowserHistory(@Path(PATH_WORD) pathword: String) : Flow<BaseResultResp<NovelBrowserResp>>

    @POST(URL.ComicAddToBookshelf)
    @FormUrlEncoded
    fun addComicToBookshelf(@Field("comic_id") comicId: String, @Field("is_collect") isCollect: Int, @Field("_update") update: Boolean = true) : Flow<BaseNullableResultResp<Any?>>

    @POST(URL.NovelAddToBookshelf)
    @FormUrlEncoded
    fun addNovelToBookshelf(@Field("book_id") novelId: String, @Field("is_collect") isCollect: Int) : Flow<BaseNullableResultResp<Any?>>

}