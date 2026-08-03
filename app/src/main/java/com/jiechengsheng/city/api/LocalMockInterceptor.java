package com.jiechengsheng.city.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Request;
import retrofit2.Invocation;

/** Offline response provider. It keeps Retrofit/BaseObserver behavior intact. */
public final class LocalMockInterceptor implements Interceptor {
    private final Gson gson = new Gson();

    @Override public Response intercept(Chain chain) {
        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);
        Type responseType = invocation == null ? null : invocation.method().getGenericReturnType();
        String body = responseJson(responseType, request.url().encodedPath());
        return new Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200)
                .message("LOCAL_MOCK").body(ResponseBody.create(MediaType.parse("application/json"), body)).build();
    }

    private String responseJson(Type returnType, String path) {
        Type dataType = Object.class;
        if (returnType instanceof ParameterizedType) {
            Type[] outer = ((ParameterizedType) returnType).getActualTypeArguments();
            if (outer.length > 0 && outer[0] instanceof ParameterizedType) {
                Type[] inner = ((ParameterizedType) outer[0]).getActualTypeArguments();
                if (inner.length > 0) dataType = inner[0];
            }
        }
        JsonObject root = new JsonObject(); root.addProperty("code", 200); root.addProperty("message", "local mock");
        root.add("data", valueFor(dataType, new HashSet<Type>(), path, "data"));
        return gson.toJson(root);
    }

    private com.google.gson.JsonElement valueFor(Type type, Set<Type> visiting, String path, String fieldName) {
        if (type == null || type == Object.class) return new JsonObject();
        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            Type raw = p.getRawType();
            if (raw instanceof Class && Collection.class.isAssignableFrom((Class<?>) raw)) {
                JsonArray a = new JsonArray(); a.add(valueFor(p.getActualTypeArguments()[0], visiting, path, fieldName)); return a;
            }
            return valueFor(raw, visiting, path, fieldName);
        }
        if (type instanceof GenericArrayType) { JsonArray a = new JsonArray(); return a; }
        if (!(type instanceof Class)) return new JsonObject();
        Class<?> c = (Class<?>) type;
        String key = fieldName.toLowerCase();
        if (c == String.class) {
            if (isImageKey(key)) return new com.google.gson.JsonPrimitive("android.resource://com.jiechengsheng.city/mipmap/ic_app_logo");
            if (key.contains("token")) return new com.google.gson.JsonPrimitive("where-rebirth-local-token");
            if (key.equals("lat") || key.contains("latitude")) return new com.google.gson.JsonPrimitive("14.5995");
            if (key.equals("lng") || key.contains("longitude")) return new com.google.gson.JsonPrimitive("120.9842");
            if (key.contains("phone")) return new com.google.gson.JsonPrimitive("09171234567");
            if (key.contains("email")) return new com.google.gson.JsonPrimitive("demo@whererebirth.local");
            if (key.contains("name") || key.contains("title") || key.contains("nickname")) return new com.google.gson.JsonPrimitive("Where Rebirth Demo");
            return new com.google.gson.JsonPrimitive(path.contains("geocode") || key.contains("address") ? "Manila, Philippines" : "Local mock content for offline preview");
        }
        if (c == boolean.class || c == Boolean.class) return new com.google.gson.JsonPrimitive(false);
        if (c == int.class || c == Integer.class || c == long.class || c == Long.class) return new com.google.gson.JsonPrimitive(key.contains("status") ? 2 : 1);
        if (c == double.class || c == Double.class || c == float.class || c == Float.class) return new com.google.gson.JsonPrimitive(key.equals("lng") || key.contains("longitude") ? 120.9842 : (key.equals("lat") || key.contains("latitude") ? 14.5995 : 1.0));
        if (c.isEnum()) return new com.google.gson.JsonPrimitive(c.getEnumConstants()[0].toString());
        if (c.isArray()) return new JsonArray();
        if (visiting.contains(c)) return new JsonObject();
        visiting.add(c); JsonObject o = new JsonObject();
        for (Field f : c.getDeclaredFields()) {
            if ((f.getModifiers() & java.lang.reflect.Modifier.STATIC) != 0 || f.isSynthetic()) continue;
            String name = f.getName(); SerializedName s = f.getAnnotation(SerializedName.class); if (s != null) name = s.value();
            o.add(name, valueFor(f.getGenericType(), visiting, path, name));
        }
        visiting.remove(c); return o;
    }

    private boolean isImageKey(String key) {
        return key.contains("image") || key.contains("avatar") || key.contains("icon") || key.contains("logo")
                || key.contains("cover") || key.contains("photo") || key.contains("picture") || key.contains("thumb");
    }
}
