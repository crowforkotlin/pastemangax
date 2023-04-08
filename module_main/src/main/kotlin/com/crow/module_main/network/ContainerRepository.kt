package com.crow.module_main.network

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: module_main/src/main/kotlin/com/crow/module_main/network
 * @Time: 2023/3/13 23:36
 * @Author: CrowForKotlin
 * @Description: ContainerRepository
 * @formatter:on
 **************************/
class ContainerRepository(private val service: ContainerService) {

    fun getUpdateInfo() = service.getUpdateInfo()

    fun getQQGroup() = service.getQQGroup()
}