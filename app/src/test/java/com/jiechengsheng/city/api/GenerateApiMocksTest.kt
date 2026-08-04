package com.jiechengsheng.city.api

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import com.google.gson.JsonParser

/** Regenerates every RetrofitApi response fixture from its declared JavaBean type. */
class GenerateApiMocksTest {
    @Test
    fun generateAllApiJson() {
        val output = File("src/main/assets/mock/api").apply { mkdirs() }
        val methods = RetrofitApi::class.java.declaredMethods.sortedWith(compareBy({ it.name }, { it.toGenericString() }))
        methods.forEach { method ->
            File(output, "${MockJsonFactory.apiKey(method)}.json")
                .writeText(MockJsonFactory.responseJson(method.genericReturnType), Charsets.UTF_8)
        }
        File(output, "_index.json").writeText(
            methods.joinToString(prefix = "[\n", postfix = "\n]\n", separator = ",\n") {
                "  {\"method\":\"${it.name}\",\"file\":\"${MockJsonFactory.apiKey(it)}.json\",\"returnType\":\"${it.genericReturnType.typeName.replace("\\", "\\\\").replace("\"", "\\\"")}\"}"
            },
            Charsets.UTF_8
        )
        assertTrue("Expected all Retrofit API methods to be generated", methods.size >= 275)
        assertTrue(File(output, "_index.json").isFile)
        output.listFiles { file -> file.extension == "json" }!!.forEach {
            JsonParser().parse(it.readText(Charsets.UTF_8))
        }
    }
}
