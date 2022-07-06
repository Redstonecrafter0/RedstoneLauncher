package net.redstonecraft.redstonelauncher.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URL
import java.net.URLEncoder
import java.time.Instant

class XBoxApi(accessToken: String, var refreshToken: String, var validUntil: Long) {

    var accessToken: String = accessToken
        get() {
            if (validUntil <= Instant.now().toEpochMilli()) {
                val response = getTokenPair(refreshToken)
                field = response.access_token
                refreshToken = response.refresh_token
                validUntil = response.expires_in * 1000 + Instant.now().toEpochMilli()
            }
            return field
        }
        set(_) = error("Can't manually reassign accessToken.")

    fun getTokenPair(refreshToken: String): RefreshTokenResponse {
        return Json.decodeFromString(postRequest("https://login.live.com/oauth20_token.srf", mapOf("client_id" to "000000004C12AE6F", "refresh_token" to refreshToken, "grant_type" to "refresh_token")))
    }

    fun postRequest(url: String, params: Map<String, String>): String {
        val conn = URL(url).openConnection()
        val data = params.map { (k, v) -> "${URLEncoder.encode(k, Charsets.UTF_8)}=${URLEncoder.encode(v, Charsets.UTF_8)}" }.joinToString("&").encodeToByteArray()
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.setRequestProperty("Content-Length", data.size.toString())
        conn.doInput = true
        conn.doOutput = true
        conn.outputStream.write(data)
        conn.outputStream.close()
        val response = conn.inputStream.readAllBytes().decodeToString()
        conn.inputStream.close()
        return response
    }

    @Serializable
    data class RefreshTokenResponse(val token_type: String, val scope: String, val expires_in: Long, val access_token: String, val refresh_token: String)

}
