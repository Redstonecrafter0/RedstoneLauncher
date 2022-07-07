package net.redstonecraft.redstonelauncher.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object AdoptiumAPI: JavaAPI {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun getVersions(): List<Int> {
        return Json.decodeFromString<AvailableReleases>(request("https://api.adoptium.net/v3/info/available_releases")).available_releases
    }

    override fun getVersion(version: Int, jdk: Boolean): JavaPackage {
        val response = json.decodeFromString<Versions>("{\"data\": ${request("https://api.adoptium.net/v3/assets/latest/$version/hotspot?architecture=x64&image_type=${if (jdk) "jdk" else "jre"}&os=${OS.current.name.lowercase()}&vendor=eclipse")}}").data.first()
        val v = response.version
        return JavaPackage(response.binary.`package`.name, response.binary.`package`.link, listOf(v.major, v.minor, v.security, v.build), response.binary.`package`.checksum, "SHA-256")
    }

    @Serializable
    data class AvailableReleases(
        val available_lts_releases: List<Int>,
        val available_releases: List<Int>,
        val most_recent_feature_release: Int,
        val most_recent_feature_version: Int,
        val most_recent_lts: Int,
        val tip_version: Int
    )

    @Serializable
    data class Versions(val data: List<Release>) {

        @Serializable
        data class Release(val binary: Binary, val version: Version) {

            @Serializable
            data class Binary(val `package`: Package) {

                @Serializable
                data class Package(
                    val checksum: String,
                    val checksum_link: String,
                    val link: String,
                    val name: String
                )
            }

            @Serializable
            data class Version(
                val major: Int,
                val minor: Int,
                val security: Int,
                val build: Int
            )
        }
    }
}
