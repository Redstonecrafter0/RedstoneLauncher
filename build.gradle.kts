import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
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
version = "0.0.1"

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
//                implementation("androidx.paging:paging-runtime:3.1.1")
//                implementation("androidx.paging:paging-compose:1.0.0-alpha15")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                implementation(kotlin("reflect"))
                implementation("com.microsoft.alm:auth-secure-storage:0.6.4") {
                    exclude(group = "net.java.dev.jna", module = "jna")
                }
                implementation("org.purejava:kdewallet:1.2.7")
                implementation("net.java.dev.jna:jna:5.12.1")
                implementation(fileTree(projectDir.resolve("libs")))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "net.redstonecraft.redstonelauncher.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.AppImage)
            packageName = "RedstoneLauncher"
            packageVersion = version.toString()
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

tasks.register("setupCef") {
    doLast {
        println("Extracting jcef natives")
        val natives = projectDir.resolve("testInstallDir/natives")
        SevenZFile(projectDir.resolve("jcef_natives.7z")).use {
            var entry: SevenZArchiveEntry? = it.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    println("Extracting ${entry.name}")
                    val buffer = ByteArray(entry.size.toInt())
                    it.read(buffer, 0, buffer.size)
                    val file = File(natives, entry.name)
                    if (file.canonicalPath.startsWith(natives.canonicalPath)) {
                        if (!(file.parentFile.exists() && file.parentFile.isDirectory)) file.parentFile.mkdirs()
                        file.writeBytes(buffer)
                    }
                }
                entry = it.nextEntry
            }
        }
        println("done")
    }
}
