package com.zen.alchan.data.network.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.zen.alchan.BuildConfig
import com.zen.alchan.data.network.apollo.adapter.CountryCodeAdapter
import com.zen.alchan.data.network.apollo.adapter.JsonAdapter
import com.zen.alchan.data.network.interceptor.HeaderInterceptor
import com.zen.alchan.type.CountryCode
import com.zen.alchan.type.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class AniListApolloHandler(
    private val headerInterceptor: HeaderInterceptor,
    private val baseUrl: String
) : ApolloHandler {

    // HTTP logging is disabled in production builds to prevent info leakage
    private val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY 
            else HttpLoggingInterceptor.Level.NONE
        )
        redactHeader("Authorization")
        redactHeader("Cookie")
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(httpLoggingInterceptor)
        .addNetworkInterceptor(headerInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override val apolloClient: ApolloClient = ApolloClient.Builder()
        .serverUrl(baseUrl)
        .okHttpClient(okHttpClient)
        .addCustomScalarAdapter(Json.type, JsonAdapter())
        .addCustomScalarAdapter(CountryCode.type, CountryCodeAdapter())
        .build()
}