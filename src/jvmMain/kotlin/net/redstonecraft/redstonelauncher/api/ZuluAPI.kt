package net.redstonecraft.redstonelauncher.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object ZuluAPI: JavaAPI {

    override fun getVersions(): List<Int> {
        return request().map { it.java_version.first() }
    }

    override fun getVersion(version: Int, jdk: Boolean): JavaPackage? {
        return try {
            val v = request(jdk).first { it.java_version.first() == version }
            JavaPackage(v.name, v.url, listOf(v.java_version[0], v.java_version.getOrElse(1) { 0 }, v.java_version.getOrElse(2) { 0 }, v.java_version.getOrElse(3) { 0 }))
        } catch (_: Throwable) {
            null
        }
    }

    fun request(jdk: Boolean = true): List<Versions.Version> {
        return Json.decodeFromString<Versions>("{\"data\": ${request("https://api.azul.com/zulu/download/community/v1.0/bundles/?os=${OS.current.name.lowercase()}&arch=x86&hw_bitness=64&ext=${when (OS.current) {
            OS.WINDOWS -> "zip"
            OS.LINUX -> "tar.gz"
        }}&bundle_type=${if (jdk) "jdk" else "jre"}&javafx=${if (jdk) "true" else "false"}&release_status=ga&latest=true")}}").data
    }

    @Serializable
    data class Versions(val data: List<Version>) {

        @Serializable
        data class Version(
            val id: Int,
            val name: String,
            val url: String,
            val java_version: List<Int>,
            val openjdk_build_number: Int,
            val jdk_version: List<Int>,
            val zulu_version: List<Int>
        )
    }
}
