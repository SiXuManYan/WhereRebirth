# The demo ships without minification. These rules keep the reflective JSON models safe if
# minification is enabled later.

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions,InnerClasses

-keep class com.jiechengsheng.city.api.request.** { *; }
-keep class com.jiechengsheng.city.api.response.** { *; }
-keep class com.jiechengsheng.city.bean.** { *; }
-keep class com.jiechengsheng.city.storage.entity.** { *; }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.jiechengsheng.city.R$* {
    public static final int *;
}

-ignorewarnings
