package com.crow.module_comic.network

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_main/src/main/kotlin/com/crow/module_main/network
 * @Time: 2023/3/13 23:36
 * @Author: CrowForKotlin
 * @Description: ContainerRepository
 * @formatter:on
 **************************/
class ComicRepository(private val service: ComicService) {

    fun getComicInfo(pathword: String) = service.getComicInfo(pathword)

    fun getComicChapter(pathword: String, start: Int, limit: Int = 100) = service.getComicChapter(pathword, start, limit)

    fun getComic(pathword: String, uuid: String) = service.getComic(pathword, uuid)
}