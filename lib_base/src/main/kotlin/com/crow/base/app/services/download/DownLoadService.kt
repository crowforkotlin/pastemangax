package com.crow.base.app.services.download

import com.crow.base.app.services.BaseService

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/com/crow/base/services
 * @Time: 2023/3/3 21:49
 * @Author: CrowForKotlin
 * @Description: DownLoadService
 * @formatter:on
 **************************/
class DownLoadService : BaseService<DownLoadBinder>() {

    override fun getBinder(): DownLoadBinder {
        return DownLoadBinder()
    }
}