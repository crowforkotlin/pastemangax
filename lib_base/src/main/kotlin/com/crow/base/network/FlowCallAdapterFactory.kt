package com.crow.base.network

import com.crow.base.extensions.callEnqueueFlow
import com.crow.base.extensions.logError
import com.crow.base.extensions.logMsg
import com.crow.base.extensions.processing
import com.crow.base.viewmodel.ViewStateException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import retrofit2.*
import java.lang.ref.WeakReference
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.net.UnknownHostException

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/cn/barry/base/network
 * @Time: 2022/4/28 13:29
 * @Author: BarryAllen
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
