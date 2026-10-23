package com.jiechengsheng.city.api

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Invocation

/**
 * 本地 Mock 拦截器：按 Retrofit 方法名读取 `assets/mock/api/<method>.json`。
 * 资产缺失或无法按方法声明的 JavaBean 解析时，使用 [MockJsonFactory] 生成兜底响应，
 * 保证旧接口在服务器停用后仍能走完整的 Retrofit/RxJava 链路。
 */
class LocalMockInterceptor(context: Context) : Interceptor {
    private val assets = context.applicationContext.assets

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val method = request.tag(Invocation::class.java)?.method()
        val key = method?.let(MockJsonFactory::apiKey)
        val assetPath = key?.let { "mock/api/$it.json" }
        val assetJson = assetPath?.let(::readAsset)
        val compatible = assetJson != null && MockJsonFactory.isCompatible(assetJson, method?.genericReturnType)
        val json = if (compatible) {
            assetJson!!
        } else {
            MockJsonFactory.responseJson(method?.genericReturnType, key ?: request.url.encodedPath)
        }

        val source = when {
            compatible -> assetPath
            assetJson != null -> "asset incompatible, generated fallback"
            else -> "generated fallback"
        }
        Log.d("LocalMock", "${request.method} ${request.url.encodedPath} -> $source")
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("LOCAL_MOCK")
            .body(json.toResponseBody(JSON))
            .build()
    }

    private fun readAsset(path: String): String? = try {
        assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
    } catch (_: Exception) {
        null
    }

    private companion object {
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    }
}
