import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("idea")
}

group = "net.redstonecraft"
version = "1.0.0"

idea {
    module {
        isDownloadSources = true
    }
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                @OptIn(ExperimentalComposeLibrary::class) implementation(compose.material3)
                implementation(compose.animationGraphics)
                implementation(compose.materialIconsExtended)
                implementation("androidx.paging:paging-runtime:3.1.1")
                implementation("androidx.paging:paging-compose:1.0.0-alpha15")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                implementation(kotlin("reflect"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "net.redstonecraft.redstonelauncher.MainKt"
        jvmArgs += listOf("-cp", "./plugins/*")
        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.AppImage)
            packageName = "RedstoneLauncher"
            packageVersion = version as String
            description = "A custom Minecraft Launcher"
            windows {
                iconFile.set(projectDir.resolve("src/jvmMain/resources/icon.ico"))
                menu = true
            }
            linux {
                iconFile.set(projectDir.resolve("src/jvmMain/resources/icon.png"))
            }
        }
    }
}
