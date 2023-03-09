package com.crow.module_home.model

import com.crow.module_home.network.HomeService

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
}