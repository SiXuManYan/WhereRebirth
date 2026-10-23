buildscript {
    repositories {
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/google/")
        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        maven("https://maven.aliyun.com/repository/jcenter/")
        maven("https://jitpack.io")
        maven("https://s01.oss.sonatype.org/content/groups/public")
        maven("https://raw.github.com/laenger/maven-releases/master/releases")
        maven("https://repo1.maven.org/maven2/")
        google()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
