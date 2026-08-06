pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/google/")
        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        maven("https://maven.aliyun.com/repository/jcenter/")
        google()
        mavenCentral()
        maven("https://jitpack.io")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/google/")
        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        maven("https://maven.aliyun.com/repository/jcenter/")
        google()
        mavenCentral()
        maven("https://jitpack.io")
        flatDir {
            dirs("$rootDir/app/libs")
        }
        maven("https://mvn.mob.com/android")
        maven("https://s01.oss.sonatype.org/content/groups/public")
    }
}

rootProject.name = "Where"
include(":app")
