pluginManagement {
    repositories {
        // 优先使用国内镜像 - 华为云
        maven("https://repo.huaweicloud.com/repository/maven/")
        maven("https://mirrors.huaweicloud.com/repository/maven/")
        // 阿里云作为备用
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/public")
        // 官方仓库作为最后备用
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.itextsupport.com/android")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "io.objectbox") {
                useModule("io.objectbox:objectbox-gradle-plugin:${requested.version}")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 优先使用国内镜像 - 华为云
        maven("https://repo.huaweicloud.com/repository/maven/")
        maven("https://mirrors.huaweicloud.com/repository/maven/")
        // 阿里云作为备用
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/public")
        // JitPack和本地仓库
        maven("https://jitpack.io")
        mavenLocal()
        // 官方仓库作为最后备用
        google()
        mavenCentral()
    }
}

rootProject.name = "rikkahub"
include(":app")
include(":highlight")
include(":ai")
include(":search")
include(":tts")
include(":common")
include(":app:baselineprofile")
include(":document")
