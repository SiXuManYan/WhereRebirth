package com.jiechengsheng.city.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Field
import java.lang.reflect.GenericArrayType
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Date
import java.util.Locale

/**
 * 按 Retrofit 返回类型生成结构合法的本地 Mock JSON。
 *
 * 数值、布尔包装类型、泛型集合、分页对象和 Android 资源图片都按字段类型处理；
 * 生成结果同时用于工程内的资产生成测试和运行时兜底。
 */
object MockJsonFactory {
    const val LIST_SIZE = 5
    private const val DATE = "2026-10-20 10:30:00"
    private val gson = GsonBuilder().setPrettyPrinting().create()

    @JvmStatic
    fun apiKey(method: Method): String {
        val overloads = method.declaringClass.declaredMethods.count { it.name == method.name }
        if (overloads == 1) return method.name
        val suffix = method.parameterTypes.joinToString("_") { sanitize(it.simpleName.ifEmpty { "arg" }) }
        return "${method.name}__${suffix.ifEmpty { "no_args" }}"
    }

    /** Retrofit 方法的 Observable<JcsResponse<T>> 中的 JcsResponse<T>。 */
    @JvmStatic
    fun responseWrapperType(returnType: Type?): Type? {
        val observable = returnType as? ParameterizedType ?: return null
        return observable.actualTypeArguments.firstOrNull()
    }

    /** 校验手写/生成 JSON 是否能按声明的 JavaBean 解析，避免运行时才抛异常。 */
    @JvmStatic
    fun isCompatible(json: String, returnType: Type?): Boolean {
        val type = responseWrapperType(returnType) ?: return true
        return try {
            gson.fromJson<Any>(json, type)
            true
        } catch (_: Exception) {
            false
        }
    }

    @JvmStatic
    fun responseJson(returnType: Type?, path: String = ""): String {
        val root = JsonObject().apply {
            addProperty("code", 200)
            addProperty("message", "local mock")
            add("data", valueFor(extractDataType(returnType), linkedSetOf(), path, "data", 0))
        }
        return gson.toJson(root)
    }

    private fun extractDataType(returnType: Type?): Type {
        val observable = returnType as? ParameterizedType ?: return Any::class.java
        val response = observable.actualTypeArguments.firstOrNull() as? ParameterizedType
            ?: return Any::class.java
        return response.actualTypeArguments.firstOrNull() ?: Any::class.java
    }

    private fun valueFor(type: Type?, visiting: MutableSet<Type>, path: String, fieldName: String, index: Int): JsonElement {
        if (type == null || type == Any::class.java || type is TypeVariable<*>) return JsonObject()
        if (type is WildcardType) return valueFor(type.upperBounds.firstOrNull(), visiting, path, fieldName, index)
        if (type is GenericArrayType) return JsonArray().apply {
            repeat(LIST_SIZE) { add(valueFor(type.genericComponentType, visiting, path, fieldName, it)) }
        }
        if (type is ParameterizedType) {
            val raw = type.rawType as? Class<*> ?: return JsonObject()
            if (Collection::class.java.isAssignableFrom(raw)) return JsonArray().apply {
                repeat(LIST_SIZE) { add(valueFor(type.actualTypeArguments[0], visiting, path, fieldName, it)) }
            }
            if (Map::class.java.isAssignableFrom(raw)) return JsonObject().apply {
                repeat(LIST_SIZE) { add("key_${it + 1}", valueFor(type.actualTypeArguments.getOrNull(1), visiting, path, fieldName, it)) }
            }
            return objectFor(raw, visiting, path, fieldName, index, type)
        }
        val clazz = type as? Class<*> ?: return JsonObject()
        return primitiveFor(clazz, path, fieldName, index)
            ?: if (clazz.isArray) JsonArray().apply {
                repeat(LIST_SIZE) { add(valueFor(clazz.componentType, visiting, path, fieldName, it)) }
            } else objectFor(clazz, visiting, path, fieldName, index, clazz)
    }

    private fun objectFor(clazz: Class<*>, visiting: MutableSet<Type>, path: String, fieldName: String, index: Int, identity: Type): JsonElement {
        if (visiting.contains(identity)) return JsonObject()
        visiting.add(identity)
        val result = JsonObject()
        val typeBindings: Map<TypeVariable<*>, Type> = if (identity is ParameterizedType) {
            clazz.typeParameters.mapIndexed { bindingIndex, variable ->
                variable as TypeVariable<*> to identity.actualTypeArguments[bindingIndex]
            }.toMap()
        } else {
            emptyMap()
        }
        allFields(clazz).forEach { field ->
            if (!Modifier.isStatic(field.modifiers) && !field.isSynthetic && !Modifier.isTransient(field.modifiers)) {
                val name = field.getAnnotation(SerializedName::class.java)?.value ?: field.name
                val resolvedType = resolveType(field.genericType, typeBindings)
                result.add(name, specialField(name, resolvedType, visiting, path, index)
                    ?: valueFor(resolvedType, visiting, path, name, index))
            }
        }
        visiting.remove(identity)
        return result
    }

    private fun specialField(name: String, type: Type, visiting: MutableSet<Type>, path: String, index: Int): JsonElement? {
        val key = name.lowercase()
        val n = index + 1
        return when {
            key == "module_type" || key == "itemtype" || key == "item_type" -> JsonPrimitive(index % 4 + 1)
            key == "current_page" || key == "last_page" || key == "from" -> JsonPrimitive(1)
            key == "per_page" || key == "to" || key == "total" -> JsonPrimitive(LIST_SIZE)
            key == "data" && rawClass(type)?.let { Collection::class.java.isAssignableFrom(it) } == true ->
                valueFor(type, visiting, path, name, index)
            key == "id" || key.endsWith("_id") -> if (isBoolean(type)) JsonPrimitive(false) else JsonPrimitive(n)
            key.contains("status") || key == "type" -> if (isBoolean(type)) JsonPrimitive(false) else JsonPrimitive(index % 2 + 1)
            else -> null
        }
    }

    private fun isBoolean(type: Type?): Boolean {
        val clazz = rawClass(type ?: return false) ?: return false
        return clazz.name == "boolean" || clazz.name == "java.lang.Boolean"
    }

    private fun primitiveFor(clazz: Class<*>, path: String, fieldName: String, index: Int): JsonElement? {
        val key = fieldName.lowercase()
        val n = index + 1
        return when (clazz.name) {
            "java.lang.String" -> JsonPrimitive(stringValue(key, path, n))
            "boolean", "java.lang.Boolean" -> JsonPrimitive(false)
            "int", "java.lang.Integer", "short", "java.lang.Short" -> JsonPrimitive(intValue(key, n))
            "long", "java.lang.Long" -> JsonPrimitive(intValue(key, n).toLong())
            "double", "java.lang.Double" -> JsonPrimitive(decimalValue(key, n))
            "float", "java.lang.Float" -> JsonPrimitive(decimalValue(key, n).toFloat())
            "byte", "java.lang.Byte" -> JsonPrimitive(intValue(key, n).toByte())
            "char", "java.lang.Character" -> JsonPrimitive("A")
            "java.math.BigDecimal" -> JsonPrimitive(BigDecimal.valueOf(decimalValue(key, n)))
            "java.math.BigInteger" -> JsonPrimitive(BigInteger.valueOf(intValue(key, n).toLong()))
            "java.util.Date" -> JsonPrimitive(DATE)
            else -> when {
                CharSequence::class.java.isAssignableFrom(clazz) -> JsonPrimitive(stringValue(key, path, n))
                JsonElement::class.java.isAssignableFrom(clazz) -> JsonObject()
                clazz.isEnum -> clazz.enumConstants?.firstOrNull()?.let { JsonPrimitive(it.toString()) } ?: JsonNull.INSTANCE
                else -> null
            }
        }
    }

    private fun stringValue(key: String, path: String, n: Int): String {
        val d = domain(path, key)
        return when {
            isImageKey(key) -> imageValue(key, d, n)
            key.contains("token") -> "where-rebirth-local-token"
            key.contains("country") -> "63"
            key.contains("verification") || key.contains("verify") -> "123456"
            key.contains("integral") || key.contains("point") -> "100"
            key.contains("uuid") -> "demo-uuid-$n"
            key == "path" -> "https://1bataan.com"
            key.contains("code") -> "200"
            key.contains("tag") -> listOf("Popular", "Recommended", "Family friendly", "Top rated", "New")[(n - 1) % 5]
            key == "lat" || key.contains("latitude") -> String.format(Locale.ROOT, "%.4f", 14.5995 + n / 1000.0)
            key == "lng" || key.contains("longitude") -> String.format(Locale.ROOT, "%.4f", 120.9842 + n / 1000.0)
            key.contains("phone") || key.contains("mobile") -> String.format(Locale.ROOT, "0917 555 %04d", 100 + n)
            key.contains("email") -> "demo$n@whererebirth.local"
            key.contains("url") || key.contains("link") -> "https://1bataan.com"
            key.contains("date") || key.endsWith("_at") || key.contains("time") -> DATE
            key.contains("address") -> addressValue(d)
            key.contains("description") || key.contains("desc") || key.contains("content") || key.contains("remark") -> descriptionValue(d)
            key.contains("name") || key.contains("title") || key.contains("nickname") -> nameValue(d, key, n)
            key.contains("grade") || key.contains("score") -> "4.5"
            key.contains("price") || key.contains("amount") || key.contains("balance") || key.contains("cost") || key.contains("fee") ->
                String.format(Locale.ROOT, "%.2f", 299.0 + n * 100)
            key.contains("distance") -> String.format(Locale.ROOT, "%.1f km", 0.8 + n * 0.4)
            key.contains("comment") || key.contains("read") || key.contains("view") || key.contains("count") -> "${120 + n * 37}"
            key.contains("star") -> "4"
            key.contains("room") -> "3"
            key.contains("area") -> "Balanga"
            key.contains("type") -> typeValue(d)
            else -> "${displayName(d)} $n"
        }
    }

    private fun decimalValue(key: String, n: Int): Double = when {
        key == "lat" || key.contains("latitude") -> 14.5995 + n / 1000.0
        key == "lng" || key.contains("longitude") -> 120.9842 + n / 1000.0
        key.contains("grade") || key.contains("score") || key.contains("star") -> 4.0 + (n % 10) / 10.0
        key.contains("distance") -> 0.8 + n * 0.4
        key.contains("price") || key.contains("amount") || key.contains("cost") || key.contains("fee") || key.contains("balance") -> 299.0 + n * 100
        key.contains("comment") || key.contains("read") || key.contains("view") -> 120.0 + n * 37
        else -> n.toDouble()
    }

    private fun intValue(key: String, n: Int): Int = when {
        key.contains("comment") || key.contains("read") || key.contains("view") || key.contains("count") -> 120 + n * 37
        key.contains("star") || key.contains("grade") -> 4
        else -> n
    }

    private fun domain(path: String, key: String): String {
        val c = "$path $key".lowercase()
        return when {
            c.contains("banner") -> "banner"
            (c.contains("user") || c.contains("login") || c.contains("account") || c.contains("profile")) && !c.contains("cv") -> "user"
            c.contains("hotel") || c.contains("room") -> "hotel"
            c.contains("food") || c.contains("restaurant") || c.contains("delicacy") || c.contains("gourmet") ||
                c.contains("dish") || c.contains("takeaway") || c.contains("meal") -> "food"
            c.contains("travel") || c.contains("tourist") || c.contains("attraction") || c.contains("scenic") -> "travel"
            c.contains("mall") || c.contains("estore") || c.contains("goods") || c.contains("product") ||
                c.contains("sku") || c.contains("coupon") || c.contains("shop") || c.contains("store") -> "mall"
            c.contains("job") || c.contains("cv") || c.contains("resume") || c.contains("employer") ||
                c.contains("company") || c.contains("career") || c.contains("certificate") -> "job"
            c.contains("news") || c.contains("article") || c.contains("publisher") -> "news"
            c.contains("bill") || c.contains("charges") || c.contains("payment") -> "bills"
            else -> "city"
        }
    }

    private fun nameValue(d: String, key: String, n: Int): String {
        val i = (n - 1).coerceAtLeast(0)
        val isTitle = key.contains("title")
        return when (d) {
            "banner" -> listOf("Discover Bataan", "Taste of the Peninsula", "Stay by the Sea")[i % 3]
            "hotel" -> listOf("Bataan Bay Resort", "Sea Breeze Hotel", "Casa Marina", "Mango Grove Inn", "The Peninsula Suites")[i % 5]
            "food" -> listOf("Seaside Grill", "Jollibee Balanga", "Cafe Bataan", "Mang Inasal", "Lola's Kitchen")[i % 5]
            "travel" -> listOf("Mount Samat", "Las Casas Filipinas", "Five Fingers Cove", "Bataan National Park", "Balanga Wetland Park")[i % 5]
            "mall" -> listOf("SM City Bataan", "Ayala Harbor Point", "Robinsons Balanga", "Bataan Public Market", "The Strip Mall")[i % 5]
            "job" -> if (isTitle) {
                listOf("Android Developer", "Marketing Specialist", "Customer Service Representative", "Accountant", "Operations Supervisor")[i % 5]
            } else {
                listOf("Where Tech", "Bataan Digital", "Harbor Logistics", "Peninsula Foods", "Blue Coast Bank")[i % 5]
            }
            "news" -> if (key.contains("nickname") || key.contains("publisher")) {
                listOf("Bataan News Desk", "Provincial Information Office", "Juan Dela Cruz", "Maria Santos", "Coast Guard Bataan")[i % 5]
            } else {
                listOf(
                    "Bataan launches coastal clean-up drive",
                    "New public market opens in Balanga",
                    "Local students win robotics competition",
                    "Free health check-up program announced",
                    "Provincial tourism arrivals hit new high",
                )[i % 5]
            }
            "user" -> listOf("Juan Dela Cruz", "Maria Santos", "Where Demo")[i % 3]
            "bills" -> listOf("Electricity Bill", "Water Bill", "Internet Bill", "Phone Bill")[i % 4]
            "city" -> listOf("Balanga", "Bataan", "Mariveles", "Dinalupihan", "Hermosa", "Orani")[i % 6]
            else -> "Where Demo $n"
        }
    }

    private fun addressValue(d: String): String = when (d) {
        "hotel", "mall", "job" -> "Balanga City, Bataan"
        "food" -> "Pilar, Bataan"
        "travel", "news" -> "Bataan, Philippines"
        else -> "Bataan, Philippines"
    }

    private fun descriptionValue(d: String): String = when (d) {
        "hotel" -> "Comfortable rooms, warm Filipino hospitality and easy access to the city."
        "food" -> "Freshly prepared local favorites served all day."
        "travel" -> "A must-visit spot for nature lovers and weekend travelers."
        "mall" -> "Everything you need in one convenient place."
        "job" -> "Join a growing team and build your career with us."
        "news" -> "Local communities come together for a brighter, cleaner Bataan."
        else -> "A local demo record for Where Rebirth."
    }

    private fun typeValue(d: String): String = when (d) {
        "food" -> "Filipino"
        "hotel" -> "Resort"
        "job" -> "Full-time"
        "mall" -> "Retail"
        else -> "Local"
    }

    private fun displayName(d: String): String = when (d) {
        "hotel" -> "Bataan Bay Resort"
        "food" -> "Seaside Grill"
        "travel" -> "Mount Samat"
        "mall" -> "SM City Bataan"
        "job" -> "Where Tech"
        "news" -> "Bataan News"
        "user" -> "Juan Dela Cruz"
        else -> "Bataan"
    }

    private fun imageValue(key: String, d: String, n: Int): String {
        val index = (n - 1) % 2 + 1
        return when {
            key.contains("avatar") || key.contains("portrait") -> resource("mock_avatar_$index")
            key.contains("icon") || key.contains("logo") -> resource("ic_module_${iconDomain(d)}")
            d == "banner" || d == "city" || d == "bills" -> resource("mock_banner_${(n - 1) % 3 + 1}")
            d == "user" -> resource("mock_avatar_$index")
            else -> resource("mock_${d}_$index")
        }
    }

    private fun iconDomain(d: String): String = when (d) {
        "hotel" -> "hotel"
        "food" -> "food"
        "travel" -> "travel"
        "mall" -> "mall"
        "job" -> "job"
        "news" -> "news"
        "bills" -> "bills"
        else -> "convenience"
    }

    private fun resource(name: String) = "android.resource://com.jiechengsheng.city/drawable/$name"

    private fun allFields(clazz: Class<*>): List<Field> = buildList {
        var current: Class<*>? = clazz
        while (current != null && current != Any::class.java) {
            addAll(current.declaredFields)
            current = current.superclass
        }
    }

    private fun rawClass(type: Type): Class<*>? = when (type) {
        is Class<*> -> type
        is ParameterizedType -> type.rawType as? Class<*>
        else -> null
    }

    private fun resolveType(type: Type, bindings: Map<TypeVariable<*>, Type>): Type = when (type) {
        is TypeVariable<*> -> bindings[type] ?: type
        is ParameterizedType -> ResolvedParameterizedType(
            type.rawType,
            type.ownerType,
            type.actualTypeArguments.map { resolveType(it, bindings) }.toTypedArray()
        )
        is GenericArrayType -> ResolvedGenericArrayType(resolveType(type.genericComponentType, bindings))
        else -> type
    }

    private data class ResolvedParameterizedType(
        private val raw: Type,
        private val owner: Type?,
        private val arguments: Array<Type>
    ) : ParameterizedType {
        override fun getRawType(): Type = raw
        override fun getOwnerType(): Type? = owner
        override fun getActualTypeArguments(): Array<Type> = arguments.clone()
    }

    private data class ResolvedGenericArrayType(private val component: Type) : GenericArrayType {
        override fun getGenericComponentType(): Type = component
    }

    private fun isImageKey(key: String) = listOf("image", "avatar", "icon", "logo", "cover", "photo", "picture", "thumb", "poster", "src", "banner", "gallery", "album").any(key::contains)
    private fun sanitize(value: String) = value.replace(Regex("[^A-Za-z0-9_]+"), "_")
}
