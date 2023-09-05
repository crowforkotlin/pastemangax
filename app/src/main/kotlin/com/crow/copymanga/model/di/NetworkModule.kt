package com.crow.copymanga.model.di

import androidx.multidex.BuildConfig
import com.crow.base.copymanga.BaseStrings
import com.crow.base.copymanga.BaseUser
import com.crow.base.copymanga.glide.AppGlideProgressFactory
import com.crow.base.copymanga.glide.AppGlideProgressResponseBody
import com.crow.base.tools.extensions.baseJson
import com.crow.base.tools.extensions.ks.asConverterFactory
import com.crow.base.tools.network.FlowCallAdapterFactory
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

val networkModule = module {

    val named_CopyMangaX = named("CopyMangaX")
    val named_ProgressGlide = named("ProgressGlide")

    /**
     * ● 默认Okhttp
     * ● 2023-06-16 21:39:31 周五 下午
     */
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().also { interceptor ->
                interceptor.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            })

            .sslSocketFactory(SSLSocketClient.sSLSocketFactory, SSLSocketClient.geX509tTrustManager())
            .hostnameVerifier(SSLSocketClient.hostnameVerifier)
            .pingInterval(10, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
        .build()
    }

    /**
     * ● 默认Retrofit
     * ● 2023-06-16 21:40:42 周五 下午
     */
    single {
        Retrofit.Builder()
            .baseUrl("https://gitee.com/")
            .client(get())
            .addCallAdapterFactory(FlowCallAdapterFactory.create())
            .addConverterFactory(baseJson.asConverterFactory("application/json; charset=utf-8".toMediaType()))
            .build()
    }

    /**
     * ● Glide进度加载 By Okhttp
     * ● 2023-06-16 21:41:59 周五 下午
     */
    single(named_ProgressGlide) {
        OkHttpClient.Builder().apply {
            addInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                val appGlideProgressFactory = AppGlideProgressFactory.getGlideProgressFactory(request.url.toString())
                if (appGlideProgressFactory == null) response
                else {
                    response.newBuilder().body(response.body?.let { AppGlideProgressResponseBody(request.url.toString(), appGlideProgressFactory.mOnProgressListener, it) }).build()
                }
            }
            sslSocketFactory(SSLSocketClient.sSLSocketFactory, SSLSocketClient.geX509tTrustManager())
            hostnameVerifier(SSLSocketClient.hostnameVerifier)
            pingInterval(10, TimeUnit.SECONDS)
            connectTimeout(15, TimeUnit.SECONDS)
            callTimeout(15, TimeUnit.SECONDS)
            readTimeout(15, TimeUnit.SECONDS)
            writeTimeout(15, TimeUnit.SECONDS)
            retryOnConnectionFailure(false)
        }.build()
    }

    /**
     * ● CopyMangaX 站点 By Okhttp
     * ● 2023-06-16 21:42:49 周五 下午
     */
    single(named_CopyMangaX) {
        OkHttpClient.Builder()

            // 动态请求地址
            .addInterceptor { chain ->
                val request = chain.request()
                chain.proceed(request.newBuilder().url(BaseStrings.URL.CopyManga.toHttpUrl().newBuilder().encodedPath(request.url.encodedPath).encodedQuery(request.url.encodedQuery).build()).build())
            }

            // 动态添加请求头
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                chain.proceed(chain.request().newBuilder()
                    .addHeader("User-Agent", "Kotlin/1.8.20 (kotlin:io)")
                    .addHeader("Platform", "1")
                    .addHeader("Authorization","Token ${BaseUser.CURRENT_USER_TOKEN}")
                    .addHeader("region", BaseUser.CURRENT_ROUTE)
                    .build()
                )
            })
            .sslSocketFactory(SSLSocketClient.sSLSocketFactory, SSLSocketClient.geX509tTrustManager())
            .hostnameVerifier(SSLSocketClient.hostnameVerifier)
            .pingInterval(5, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
        .build()
    }

    /**
     * ● CopyMangaX 站点 By Retrofit
     * ● 2023-06-16 21:43:36 周五 下午
     */
    single(named_CopyMangaX) {
        Retrofit.Builder()
            .baseUrl(BaseStrings.URL.CopyManga)
            .client(get(named_CopyMangaX))
            .addCallAdapterFactory(FlowCallAdapterFactory.create())
            .addConverterFactory(baseJson.asConverterFactory("application/json; charset=utf-8".toMediaType()))
            .build()
    }
}
