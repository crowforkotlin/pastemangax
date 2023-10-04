package com.crow.module_main.network

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_main.network
 * @Time: 2023/10/4 14:32
 * @Author: CrowForKotlin
 * @Description: AppRepository
 * @formatter:on
 **************************/
class AppRepository(val service: AppService) {

    fun getUpdateInfo() = service.getUpdateInfo()

    fun getQQGroup() = service.getQQGroup()

    fun getSite() = service.getSite()
}