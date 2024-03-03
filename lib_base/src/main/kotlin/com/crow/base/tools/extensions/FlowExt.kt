package com.crow.base.tools.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.crow.base.BuildConfig
import com.crow.base.ui.viewmodel.ViewStateException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.UnknownHostException

/*************************
 * @Machine: RedmiBook Pro 15 Win11
 * @Path: lib_base/src/main/java/cn/barry/base/network
 * @Time: 2022/5/1 13:54
 * @Author: CrowForKotlin
 * @Description: Flow扩展
 * @formatter:off
 **************************/

// 异步请求
internal fun <R> ProducerScope<R>.callEnqueueFlow(call: Call<R>) {
    call.enqueue(object : Callback<R> {
        override fun onResponse(call: Call<R>, response: Response<R>) {
            processing(response)
        }

        override fun onFailure(call: Call<R>, t: Throwable) {
            if (BuildConfig.DEBUG) t.stackTraceToString().error()
            if (t is UnknownHostException) { close(ViewStateException("解析地址错误！请检查您的网络！", t)) }
            close(t)
        }
    })
}

// 同步请求
internal fun <R> ProducerScope<R>.callFlow(call: Call<R>) {
    runCatching {
        processing(call.execute())
    }.onFailure {
        cancel(CancellationException(it.localizedMessage, it))
    }
}

internal fun <R> ProducerScope<R>.processing(response: Response<R>) {
    //HttpCode 为 200
    if (response.isSuccessful) {
        val body = response.body()
        val code = response.code()
        // 204: 执行成功但是没有返回数据
        if (body == null || code == 204) {
            cancel(CancellationException("HTTP status code: $code"))
        } else {
            trySendBlocking(body)
                .onSuccess { close() }
                .onClosed { cancel(CancellationException(it?.localizedMessage, it)) }
                .onFailure { cancel(CancellationException(it?.localizedMessage, it)) }
        }
    } else {
        if (response.code() == 403) cancel(CancellationException("请求次数过多，服务器拒绝访问，请等待一段时间再来吧！"))
        else {
            val msg = response.errorBody()?.string()
            cancel(CancellationException((msg ?: response.message())))
        }
    }
}

suspend fun <T> Flow<T?>.onNotnull(notNull: suspend (T) -> Unit) {
    collect { if (it != null) notNull(it) }
}

suspend inline fun <T> Flow<T>.toDataNullable(): T? {
    var data: T? = null
    collect {
        data = it
    }
    return data
}

fun interface IBaseFlowCollectLifecycle<T> {
    suspend fun onCollect(value : T)
}
fun<T> Flow<T>.onCollect(fragment: Fragment, lifecycleState: Lifecycle.State = Lifecycle.State.STARTED, iBaseFlowCollectLifecycle: IBaseFlowCollectLifecycle<T>) {
    fragment.repeatOnLifecycle(lifecycleState) { collect { iBaseFlowCollectLifecycle.onCollect(it) } }
}

fun<T> Flow<T>.onCollect(activity: AppCompatActivity, lifecycleState: Lifecycle.State = Lifecycle.State.STARTED, iBaseFlowCollectLifecycle: IBaseFlowCollectLifecycle<T>) {
    activity.repeatOnLifecycle(lifecycleState) { collect { iBaseFlowCollectLifecycle.onCollect(it) } }
}