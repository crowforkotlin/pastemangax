package com.crow.module_book.network

import com.crow.base.tools.extensions.safeAs
import com.crow.base.tools.extensions.toTypeEntity
import com.crow.base.tools.extensions.toast
import com.crow.mangax.copymanga.resp.BaseResultResp
import com.crow.module_book.model.resp.ComicPageResp
import kotlinx.coroutines.flow.Flow
import okhttp3.internal.http.HTTP_OK

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_main/src/main/kotlin/com/crow/module_main/network
 * @Time: 2023/3/13 23:36
 * @Author: CrowForKotlin
 * @Description: ContainerRepository
 * @formatter:on
 **************************/
class BookRepository(private val service: ComicService) {

    fun getComicInfo(pathword: String) = service.getComicInfo(pathword)

    fun getComicChapter(pathword: String, start: Int, limit: Int = 100) = service.getComicChapter(pathword, start, limit)

    fun getComicPage(pathword: String, uuid: String) = service.getComicPage(pathword, uuid)

    fun getComicBrowserHistory(pathword: String) = service.getComicBrowserHistory(pathword)

    fun getNovelInfo(pathword: String) = service.getNovelInfo(pathword)

    fun getNovelPage(novelPageUrl: String) = service.getNovelPage(novelPageUrl)

    fun getNovelChapter(pathword: String) = service.getNovelChapter(pathword)

    fun getNovelCatelogue(pathword: String) = service.getNovelCatelogue(pathword)

    fun getNovelBrowserHistory(pathword: String) = service.getNovelBrowserHistory(pathword)

    fun addComicToBookshelf(comicId: String, isCollect: Int) = service.addComicToBookshelf(comicId, isCollect)

    fun addNovelToBookshelf(novelId: String, isCollect: Int) = service.addNovelToBookshelf(novelId, isCollect)
}