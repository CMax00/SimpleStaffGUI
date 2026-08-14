pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.nexomc.com/releases")
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.nexomc.com/releases")
    }
}

rootProject.name = "SimpleStaffGUI"
