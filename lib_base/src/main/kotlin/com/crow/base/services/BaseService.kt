package com.crow.base.services

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/cn/barry/base/services
 * @Time: 2022/5/8 20:16
 * @Author: CrowForKotlin
 * @Description: BaseService
 * @formatter:off
 **************************/

abstract class BaseService<T: Binder> : LifecycleService() {

    private val mBinder by lazy { getBinder() }

    abstract fun getBinder(): T

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return mBinder
    }

}