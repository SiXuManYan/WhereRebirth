package com.jiechengsheng.city.api

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Invocation

/** Serves the JSON generated in assets/mock/api without contacting the retired server. */
class LocalMockInterceptor(context: Context) : Interceptor {
    private val assets = context.applicationContext.assets

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val method = request.tag(Invocation::class.java)?.method()
        val key = method?.let(MockJsonFactory::apiKey)
        val assetPath = key?.let { "mock/api/$it.json" }
        val json = assetPath?.let(::readAsset)
            ?: MockJsonFactory.responseJson(method?.genericReturnType, request.url().encodedPath())

        Log.d("LocalMock", "${request.method()} ${request.url().encodedPath()} -> ${assetPath ?: "generated fallback"}")
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("LOCAL_MOCK")
            .body(ResponseBody.create(JSON, json))
            .build()
    }

    private fun readAsset(path: String): String? = try {
        assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
    } catch (_: Exception) {
        null
    }

    private companion object {
        val JSON: MediaType? = MediaType.parse("application/json; charset=utf-8")
    }
}
