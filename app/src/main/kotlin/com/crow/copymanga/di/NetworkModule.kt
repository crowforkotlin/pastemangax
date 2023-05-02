package com.crow.copymanga.di

import com.crow.base.BuildConfig
import com.crow.base.current_project.BaseStrings
import com.crow.base.current_project.BaseUser
import com.crow.base.tools.extensions.baseMoshi
import com.crow.base.tools.network.FlowCallAdapterFactory
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        OkHttpClient.Builder().apply {
            addInterceptor(HttpLoggingInterceptor().also { interceptor ->
                interceptor.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            })

            // 动态修改URL
            addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val request = chain.request()
                if (!request.url.host.contains("copymanga")) chain.proceed(request)
                else {
                    val newRequset = request.newBuilder().url(BaseStrings.URL.CopyManga.toHttpUrl().newBuilder().encodedPath(request.url.encodedPath).encodedQuery(request.url.encodedQuery).build()).build()
                    chain.proceed(newRequset)
                }
            })

            // 动态添加请求头
            addInterceptor(Interceptor { chain: Interceptor.Chain ->
                chain.proceed(chain.request().newBuilder()
                    .addHeader("User-Agent", "Dart/2.16 (dart:io)")
                    .addHeader("Platform", "1")
                    .addHeader("Authorization","Token ${BaseUser.CURRENT_USER_TOKEN}")
                    .addHeader("region", BaseUser.CURRENT_REGION)
                    .build()
                )
            })
            pingInterval(10, TimeUnit.SECONDS)
            connectTimeout(5, TimeUnit.SECONDS)
            callTimeout(10, TimeUnit.SECONDS)
            readTimeout(15, TimeUnit.SECONDS)
            writeTimeout(15, TimeUnit.SECONDS)
            retryOnConnectionFailure(false)
        }.build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BaseStrings.URL.CopyManga)
            .client(get())
            .addCallAdapterFactory(FlowCallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(baseMoshi))
            .build()
    }
}
