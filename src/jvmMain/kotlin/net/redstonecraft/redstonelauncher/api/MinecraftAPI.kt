package net.redstonecraft.redstonelauncher.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

class MinecraftAPI(private val accessToken: String) {

    fun getProfile(): MCProfile? {
        val resp = request("https://api.minecraftservices.com/minecraft/profile")
        return if (resp == null) {
            null
        } else {
            Json.decodeFromString<MCProfile>(resp)
        }
    }

    private fun request(url: String): String? {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", "Bearer $accessToken")
        return if (conn.responseCode == 404) {
            null
        } else {
            conn.inputStream.readAllBytes().decodeToString()
        }
    }

    @Serializable
    data class MCProfile(val id: String, val name: String, val skins: List<Skin>, val capes: List<Cape>) {

        @Serializable
        data class Skin(val id: String, val state: String, val url: String, val variant: String, val alias: String = "")

        @Serializable
        data class Cape(val id: String, val state: String, val url: String, val alias: String)
    }
}
