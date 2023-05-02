package com.crow.base.tools.network

import com.crow.base.tools.extensions.callEnqueueFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type


/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/cn/barry/base/network
 * @Time: 2022/5/1 13:53
 * @Author: CrowForKotlin
 * @Description:
 **************************/
class FlowCallAdapter<R>(private val responseType: Type) : CallAdapter<R, Flow<R?>> {

    override fun responseType(): Type = responseType

    override fun adapt(call: Call<R>): Flow<R?> {
        return callbackFlow<R> {
            callEnqueueFlow(call)
            awaitClose { call.cancel() }
        }
    }
}