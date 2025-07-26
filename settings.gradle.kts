pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()

        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url= uri("https://jitpack.io")
        }
        jcenter()
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = "mapbox"


                password ="sk.eyJ1IjoibWluZC1ieXRlIiwiYSI6ImNtNnAwcnh0NzE4NG0yaXNob3ZubzMxOGwifQ.Blz73LlYJNxScJv_48VkxA"//live
//                password ="sk.eyJ1IjoidGhlZGV2aWw1NTIyMTEiLCJhIjoiY200d2Rkbnp2MGFzNTJqc2Z3Y281YzU5ZiJ9.jkeh7l2DyZj0sG4_dlLxvg" //test
            }
        }
//       maven{
//           url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
//       }

    }
}

rootProject.name = "MBGpsVoiceNavigation"
include(":app")
 