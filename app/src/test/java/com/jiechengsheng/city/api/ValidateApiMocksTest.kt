package com.jiechengsheng.city.api

import com.google.gson.Gson
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** 校验每份资产 JSON 都能按 Retrofit 方法声明的 JavaBean 解析。 */
class ValidateApiMocksTest {
    @Test
    fun fixturesMatchDeclaredBeans() {
        val gson = Gson()
        val errors = mutableListOf<String>()
        RetrofitApi::class.java.declaredMethods.forEach { method ->
            val file = File("src/main/assets/mock/api/${MockJsonFactory.apiKey(method)}.json")
            if (!file.isFile) {
                errors += "missing ${file.name}"
                return@forEach
            }
            val type = MockJsonFactory.responseWrapperType(method.genericReturnType) ?: return@forEach
            try {
                gson.fromJson<Any>(file.readText(Charsets.UTF_8), type)
            } catch (e: Exception) {
                errors += "${file.name}: ${e.message}"
            }
        }
        assertTrue("Invalid mock fixtures:\n" + errors.joinToString("\n"), errors.isEmpty())
    }
}
