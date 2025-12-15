pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // --- INI YANG WAJIB DITAMBAHKAN ---
        maven { url = uri("https://jitpack.io") }
        // ----------------------------------
    }
}

rootProject.name = "TemuSkill"
include(":app")