package net.redstonecraft.redstonelauncher.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.min

object Modpacks {

    fun search(count: Int, offset: Int, query: String): List<ModpackInfo> {
        val response = Json.decodeFromString<ModpacksSearch>(URL("https://api.modpacks.ch/public/modpack/search/${count + offset}?term=${URLEncoder.encode(query, StandardCharsets.UTF_8)}").readText())
        return response.curseforge.subList(offset, min(count + 1, response.curseforge.size))
            .map { Json.decodeFromString<ModpacksData>(URL("https://api.modpacks.ch/public/curseforge/${it}").readText()) }
            .map {
                val release = it.versions.filter { i -> i.type == "Release" }.maxByOrNull { i -> i.updated }
                val beta = it.versions.filter { i -> i.type == "Beta" }.maxByOrNull { i -> i.updated }
                val alpha = it.versions.filter { i -> i.type == "Alpha" }.maxByOrNull { i -> i.updated }
                ModpackInfo(
                    it.id,
                    it.name,
                    it.description,
                    it.art.firstOrNull()?.url,
                    it.tags.map { i -> i.name },
                    if (release != null) release.id to release.updated else null,
                    if (beta != null) beta.id to beta.updated else null,
                    if (alpha != null) alpha.id to alpha.updated else null
                )
            }
    }

    fun getLatest(modpack: ModpackInfo): ModpackVersion {
        return Json.decodeFromString(URL("https://api.modpacks.ch/public/curseforge/${modpack.id}/${modpack.latestRelease?.first ?: modpack.latestBeta?.first ?: modpack.latestAlpha?.first ?: 0}").readText())
    }

}

@Serializable
data class ModpacksSearch(
    val packs: List<Int>,
    val curseforge: List<Int>,
    val total: Int,
    val limit: Int,
    val refreshed: Long
)

@Serializable
data class ModpacksData(
    val synopsis: String,
    val description: String,
    val art: List<Art>,
    val links: List<Link>,
    val authors: List<Author>,
    val versions: List<Version>,
    val installs: Int,
    val plays: Int,
    val tags: List<Tag>,
    val featured: Boolean,
    val refreshed: Long,
    val notification: String,
    val rating: Rating,
    val status: String,
    val released: Long,
    val plays_14d: Int,
    val id: Int,
    val name: String,
    val type: String,
    val updated: Long
) {

    @Serializable
    data class Art(
        val width: Int,
        val height: Int,
        val compressed: Boolean,
        val url: String,
        val mirrors: List<String>,
        val sha1: String,
        val size: Int,
        val id: Int,
        val type: String,
        val updated: Long
    )

    @Serializable
    data class Link(
        val id: Int,
        val name: String,
        val link: String,
        val type: String
    )

    @Serializable
    data class Author(
        val website: String,
        val id: Int,
        val name: String,
        val type: String,
        val updated: Long
    )

    @Serializable
    data class Version(
        val targets: List<Target>,
        val id: Int,
        val name: String,
        val type: String,
        val updated: Long
    ) {

        @Serializable
        data class Target(
            val version: String,
            val id: Int,
            val name: String,
            val type: String,
            val updated: Long
        )
    }

    @Serializable
    data class Tag(
        val id: Int,
        val name: String
    )

    @Serializable
    data class Rating(
        val id: Int,
        val configured: Boolean,
        val verified: Boolean,
        val age: Int,
        val gambling: Boolean,
        val frightening: Boolean,
        val alcoholdrugs: Boolean,
        val nuditysexual: Boolean,
        val sterotypeshate: Boolean,
        val language: Boolean,
        val violence: Boolean
    )
}

@Serializable
data class ModpackVersion(
    val files: List<Files>,
    val targets: List<Target>,
    val installs: Int,
    val plays: Int,
    val refreshed: Long,
    val changelog: String,
    val parent: Int,
    val notification: String,
    val links: List<Link>,
    val status: String,
    val id: Int,
    val name: String,
    val type: String,
    val updated: Long,
    val released: Long
) {

    @Serializable
    data class Files(
        val version: Int,
        val path: String,
        val url: String,
        val mirrors: List<String>,
        val sha1: String,
        val size: Int,
        val tags: List<String>,
        val clientonly: Boolean,
        val serveronly: Boolean,
        val optional: Boolean,
        val id: Int,
        val name: String,
        val type: String,
        val updated: Long,
        val curseforge: CurseForge = CurseForge(-1, -1)
    ) {

        val dl_url by lazy { if (url != "") url else "https://edge.forgecdn.net/files/${id.toString().substring(0, 4)}/${id.toString().substring(4)}/${name}" }

        fun getTargetLocation(root: File) = root.resolve(path).resolve(name)

        @Serializable
        data class CurseForge(
            val project: Int,
            val file: Int
        )
    }

    @Serializable
    data class Target(
        val version: String,
        val id: Int,
        val name: String,
        val type: String,
        val updated: Long
    )

    @Serializable
    data class Link(
        val id: Int,
        val name: String,
        val link: String,
        val type: String
    )
}

data class ModpackInfo(
    val id: Int,
    val name: String,
    val description: String,
    val image: String?,
    val tags: List<String>,
    val latestRelease: Pair<Int, Long>?,
    val latestBeta: Pair<Int, Long>?,
    val latestAlpha: Pair<Int, Long>?
)
