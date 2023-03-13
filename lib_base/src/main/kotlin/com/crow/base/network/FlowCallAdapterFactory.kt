package com.crow.base.network

import kotlinx.coroutines.flow.Flow
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.ref.WeakReference
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/cn/barry/base/network
 * @Time: 2022/4/28 13:29
 * @Author: CrowForKotlin
 * @Description:
 * @formatter:off
 **************************/

class FlowCallAdapterFactory private constructor() : CallAdapter.Factory() {

    companion object { fun create() = FlowCallAdapterFactory() }

    private val mFLow = WeakReference(Flow::class.java)

    override fun get(returnType: Type, annotations: Array<out Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if(getRawType(returnType) != mFLow.get()) return null
        return FlowCallAdapter<Any>(getParameterUpperBound(0, returnType as ParameterizedType))
    }
}
