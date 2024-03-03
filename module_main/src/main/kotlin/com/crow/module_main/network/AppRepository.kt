package com.crow.module_main.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Package: com.crow.module_main.network
 * @Time: 2023/10/4 14:32
 * @Author: CrowForKotlin
 * @Description: AppRepository
 * @formatter:on
 **************************/
class AppRepository(val service: AppService) {

    fun getUpdateHistory() = service.getUpdateHistory().flowOn(Dispatchers.IO)

    fun getUpdateInfo() = service.getUpdateInfo().flowOn(Dispatchers.IO)

    fun getQQGroup() = service.getGroup().flowOn(Dispatchers.IO)

    fun getSite() = service.getSite().flowOn(Dispatchers.IO)
}