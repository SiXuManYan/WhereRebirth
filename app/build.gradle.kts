import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.xml.parsers.DocumentBuilderFactory
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.w3c.dom.Element

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

apply(from = "mob.gradle")

fun releaseTime(): String = SimpleDateFormat("yyyyMMdd", Locale.ROOT).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}.format(Date())

val legacyViewAccessorsDir = layout.buildDirectory.dir("generated/source/legacyViewAccessors")

val generateLegacyViewAccessors by tasks.registering {
    val layoutFiles = fileTree("src/main/res") {
        include("layout*/**/*.xml")
    }
    inputs.files(layoutFiles)
    outputs.dir(legacyViewAccessorsDir)

    doLast {
        val outputRoot = legacyViewAccessorsDir.get().asFile
        outputRoot.deleteRecursively()
        outputRoot.mkdirs()

        val parser = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val layoutsByName = layoutFiles.files
            .sortedBy { it.invariantSeparatorsPath }
            .associateBy { it.nameWithoutExtension }

        fun viewType(element: Element, visitedLayouts: Set<String>): String {
            return when (val tag = element.tagName) {
                "View" -> "android.view.View"
                "ViewStub" -> "android.view.ViewStub"
                "SurfaceView" -> "android.view.SurfaceView"
                "TextureView" -> "android.view.TextureView"
                "WebView" -> "android.webkit.WebView"
                "fragment", "merge" -> "android.view.View"
                "view" -> element.getAttribute("class").ifBlank { "android.view.View" }
                "include" -> {
                    val includedLayout = element.getAttribute("layout").substringAfter("@layout/", "")
                    if (includedLayout.isBlank() || includedLayout in visitedLayouts) {
                        "android.view.View"
                    } else {
                        val includedFile = layoutsByName[includedLayout]
                        if (includedFile == null) {
                            "android.view.View"
                        } else {
                            val root = parser.parse(includedFile).documentElement
                            viewType(root, visitedLayouts + includedLayout)
                        }
                    }
                }
                else -> if ('.' in tag) tag else "android.widget.$tag"
            }
        }

        fun collectViews(
            layoutName: String,
            visitedLayouts: Set<String> = emptySet(),
        ): LinkedHashMap<String, String> {
            if (layoutName in visitedLayouts) return linkedMapOf()
            val layoutFile = layoutsByName[layoutName] ?: return linkedMapOf()
            val document = parser.parse(layoutFile)
            val views = linkedMapOf<String, String>()
            val elements = document.getElementsByTagName("*")

            for (index in 0 until elements.length) {
                val element = elements.item(index) as Element
                val id = element.getAttribute("android:id")
                    .removePrefix("@+id/")
                    .removePrefix("@id/")
                if (id.isNotBlank()) {
                    views.putIfAbsent(id, viewType(element, visitedLayouts + layoutName))
                }

                if (element.tagName == "include") {
                    val includedLayout = element.getAttribute("layout").substringAfter("@layout/", "")
                    if (includedLayout.isNotBlank()) {
                        collectViews(includedLayout, visitedLayouts + layoutName).forEach(views::putIfAbsent)
                    }
                }
            }
            return views
        }

        val allViews = linkedMapOf<String, String>()
        layoutsByName.keys.sorted().forEach { layoutName ->
            collectViews(layoutName).forEach(allViews::putIfAbsent)
        }

        val source = buildString {
            appendLine("@file:Suppress(\"unused\")")
            appendLine()
            appendLine("package com.jiechengsheng.city.legacyviews")
            appendLine()
            appendLine("import com.jiechengsheng.city.R")
            appendLine()
            allViews.forEach { (id, type) ->
                appendLine("val android.app.Activity.`$id`: $type get() = findViewById(R.id.$id)")
                appendLine("val androidx.fragment.app.Fragment.`$id`: $type get() = requireView().findViewById(R.id.$id)")
                appendLine("val android.view.View.`$id`: $type get() = findViewById(R.id.$id)")
                appendLine()
            }
        }
        outputRoot.resolve("LegacyViewAccessors.kt").writeText(source)
    }
}

android {
    namespace = "com.jiechengsheng.city"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jiechengsheng.city"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

        ndk {
            abiFilters += setOf("armeabi", "armeabi-v7a", "arm64-v8a")
        }

        manifestPlaceholders += mapOf(
            "JPUSH_PKGNAME" to applicationId!!,
            "JPUSH_APPKEY" to "c1845048016655585610609e",
            "JPUSH_CHANNEL" to "default_developer",
        )
    }

    flavorDimensions += "versionCode"

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
        release {
            isDebuggable = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    packaging {
        jniLibs.keepDebugSymbols += setOf("**/mips/*.so", "**/mips64/*.so")
    }

    applicationVariants.all {
        outputs.all {
            (this as BaseVariantOutputImpl).outputFileName =
                "Where_v${defaultConfig.versionCode}_(${defaultConfig.versionName})_${releaseTime()}.apk"
        }
    }

    productFlavors {
        create("dev") {
            buildConfigField("String", "SERVER_HOST", providers.gradleProperty("SERVER_HOST_TEST").get())
            buildConfigField("String", "GOOGLE_CLOUD_API_KEY", providers.gradleProperty("GOOGLE_CLOUD_API_KEY").get())
            buildConfigField("String", "GOOGLE_CLOUD_OAUTH_CLIENT_ID", providers.gradleProperty("GOOGLE_CLOUD_OAUTH_CLIENT_ID").get())
        }

        create("formal") {
            buildConfigField("String", "SERVER_HOST", providers.gradleProperty("SERVER_HOST_RELEASE").get())
            buildConfigField("String", "GOOGLE_CLOUD_API_KEY", providers.gradleProperty("GOOGLE_CLOUD_API_KEY").get())
            buildConfigField("String", "GOOGLE_CLOUD_OAUTH_CLIENT_ID", providers.gradleProperty("GOOGLE_CLOUD_OAUTH_CLIENT_ID").get())
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDir(legacyViewAccessorsDir)
            jniLibs.srcDirs("libs")
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(generateLegacyViewAccessors)
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.base.recycler.view.adapter.helper)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.core)
    implementation(libs.utilcodex)
    implementation(libs.retrofit.adapter.rxjava2)
    implementation(libs.rxjava2)
    implementation(libs.rxandroid2)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.material)
    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)

    implementation(libs.android.gif.drawable)
    implementation(libs.glide)
    kapt(libs.glide.compiler)
    implementation(libs.ptr.load.more)
    implementation(libs.marquee.library)
    implementation(libs.rounded.image.view)
    implementation(libs.picasso)
    implementation(libs.pinyin4j)
    implementation(libs.circle.image.view)
    implementation(libs.tiny.pinyin)
    implementation(libs.play.services.maps)
    implementation(libs.android.maps.utils)
    implementation(libs.play.services.location)
    implementation(libs.eventbus)
    implementation(libs.androidx.multidex)
    implementation(libs.universal.image.loader)
    implementation(libs.rebound)
    implementation(libs.agentweb)
    implementation(libs.tkrefreshlayout)
    implementation(libs.sliding.up.panel)
    implementation(libs.xbanner)
    implementation(libs.range.seek.bar)
    implementation(libs.calendar.view)
    implementation(libs.status.bar.util)
    implementation(libs.keyboard.visibility.event)
    implementation(libs.jiaozi.video.player)
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.jpush.google)
    implementation(libs.jcore.google)
    implementation(libs.matisse)
    implementation(libs.rxpermissions2)
    implementation(libs.flyco.tab.layout)
    implementation(libs.gsy.video.player.java)
    implementation(libs.gsy.video.player.arm64)
    implementation(libs.gsy.video.player.armv7a)
    implementation(libs.banner)
    implementation(libs.expandable.text.view)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.smart.refresh.layout.kernel)
    implementation(libs.maps.ktx)
    implementation(libs.maps.utils.ktx)
    implementation(libs.vpbs)
    implementation(libs.pinned.section.item.decoration)
    implementation(libs.luban)
    implementation(libs.facebook.applinks)
    implementation(libs.photo.view)
    implementation(libs.bottom.dialog)
    implementation(libs.zxing.core)
}
