import java.net.URL
import com.google.gson.JsonParser
import com.google.gson.internal.bind.util.ISO8601Utils
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.text.ParsePosition

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
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
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
            appResourcesRootDir.set(projectDir.resolve("resources"))
            modules("jdk.httpserver", "jdk.security.auth")
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

tasks.register("setupEnvironment") {
    doLast {
        println("Creating Test Environment")
        val testInstallDir = projectDir.resolve("testInstallDir")
        if (testInstallDir.exists() && testInstallDir.isDirectory) {
            testInstallDir.deleteRecursively()
        }
        testInstallDir.resolve("plugins").mkdirs()
        val os = System.getProperty("os.name").toLowerCase()
        val resourcesDir = projectDir.resolve("resources/${when {
            "win" in os -> "windows"
            "linux" in os -> "linux"
            else -> error("Unknown system")
        }}")
        if (resourcesDir.exists() && resourcesDir.isDirectory) {
            resourcesDir.deleteRecursively()
        }
        resourcesDir.mkdirs()
        val msaLoginDownloadUrl = JsonParser.parseString(URL("https://api.github.com/repos/Redstonecrafter0/RedstoneLauncher-MSA-Frontend/releases").readText())
            .asJsonArray.filter { !it.asJsonObject["prerelease"].asBoolean }
            .maxByOrNull { ISO8601Utils.parse(it.asJsonObject["published_at"].asString, ParsePosition(0)).time }!!
            .asJsonObject["assets"].asJsonArray.find {
            when {
                "win" in os -> "win32"
                "linux" in os -> "linux"
                else -> error("Unknown system")
            } in it.asJsonObject["name"].asString }!!
            .asJsonObject["browser_download_url"].asString
        TarArchiveInputStream(GzipCompressorInputStream(URL(msaLoginDownloadUrl).openStream())).use {
            var entry = it.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    println("Extracting ${entry.name}")
                    val buffer = ByteArray(entry.size.toInt())
                    it.read(buffer, 0, buffer.size)
                    val file = File(resourcesDir, entry.name)
                    if (file.canonicalPath.startsWith(resourcesDir.canonicalPath)) {
                        if (!(file.parentFile.exists() && file.parentFile.isDirectory)) file.parentFile.mkdirs()
                        file.writeBytes(buffer)
                    }
                }
                entry = it.nextEntry
            }
        }
        if ("linux" in os) {
            resourcesDir.resolve("RedstoneLauncher-MSA-Login-linux-x64/RedstoneLauncher-MSA-Login").setExecutable(true)
        }
        println("done")
    }
}
