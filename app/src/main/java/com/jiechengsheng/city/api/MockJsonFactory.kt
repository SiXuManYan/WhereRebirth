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

/** Creates deterministic, editable mock payloads from Retrofit return JavaBean types. */
object MockJsonFactory {
    const val LIST_SIZE = 5
    private val gson = GsonBuilder().setPrettyPrinting().create()

    @JvmStatic
    fun apiKey(method: Method): String {
        val overloads = method.declaringClass.declaredMethods.count { it.name == method.name }
        if (overloads == 1) return method.name
        val suffix = method.parameterTypes.joinToString("_") { sanitize(it.simpleName.ifEmpty { "arg" }) }
        return "${method.name}__${suffix.ifEmpty { "no_args" }}"
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
            key == "id" || key.endsWith("_id") -> JsonPrimitive(n)
            key.contains("status") || key == "type" -> JsonPrimitive(index % 2 + 1)
            else -> null
        }
    }

    private fun primitiveFor(clazz: Class<*>, path: String, fieldName: String, index: Int): JsonElement? {
        val key = fieldName.lowercase()
        val n = index + 1
        return when {
            clazz == String::class.java || CharSequence::class.java.isAssignableFrom(clazz) -> JsonPrimitive(stringValue(key, path, n))
            clazz == Boolean::class.java || clazz == Boolean::class.javaPrimitiveType -> JsonPrimitive(false)
            clazz == Int::class.java || clazz == Int::class.javaPrimitiveType || clazz == Short::class.java || clazz == Short::class.javaPrimitiveType -> JsonPrimitive(n)
            clazz == Long::class.java || clazz == Long::class.javaPrimitiveType -> JsonPrimitive(n.toLong())
            clazz == Double::class.java || clazz == Double::class.javaPrimitiveType -> JsonPrimitive(decimalValue(key, n))
            clazz == Float::class.java || clazz == Float::class.javaPrimitiveType -> JsonPrimitive(decimalValue(key, n).toFloat())
            clazz == Byte::class.java || clazz == Byte::class.javaPrimitiveType -> JsonPrimitive(n.toByte())
            clazz == Char::class.java || clazz == Char::class.javaPrimitiveType -> JsonPrimitive("A")
            clazz == BigDecimal::class.java -> JsonPrimitive(BigDecimal("${n}9.90"))
            clazz == BigInteger::class.java -> JsonPrimitive(BigInteger.valueOf(n.toLong()))
            clazz == Date::class.java -> JsonPrimitive("2026-08-04 12:00:00")
            JsonElement::class.java.isAssignableFrom(clazz) -> JsonObject()
            clazz.isEnum -> clazz.enumConstants?.firstOrNull()?.let { JsonPrimitive(it.toString()) } ?: JsonNull.INSTANCE
            else -> null
        }
    }

    private fun stringValue(key: String, path: String, n: Int): String = when {
        isImageKey(key) -> "android.resource://com.jiechengsheng.city/mipmap/ic_app_logo"
        key.contains("token") -> "where-rebirth-local-token"
        key == "lat" || key.contains("latitude") -> "14.5995"
        key == "lng" || key.contains("longitude") -> "120.9842"
        key.contains("phone") || key.contains("mobile") -> "0917123456$n"
        key.contains("email") -> "demo$n@whererebirth.local"
        key.contains("url") || key.contains("link") -> "https://example.com/local/$n"
        key.contains("date") || key.endsWith("_at") || key.contains("time") -> "2026-08-04 12:00:00"
        key.contains("address") || path.contains("geocode") -> "Manila, Philippines"
        key.contains("name") || key.contains("title") || key.contains("nickname") -> "Where Rebirth Demo $n"
        else -> "Local mock $n"
    }

    private fun decimalValue(key: String, n: Int): Double = when {
        key == "lat" || key.contains("latitude") -> 14.5995 + n / 1000.0
        key == "lng" || key.contains("longitude") -> 120.9842 + n / 1000.0
        else -> n.toDouble()
    }

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

    private fun isImageKey(key: String) = listOf("image", "avatar", "icon", "logo", "cover", "photo", "picture", "thumb").any(key::contains)
    private fun sanitize(value: String) = value.replace(Regex("[^A-Za-z0-9_]+"), "_")
}
