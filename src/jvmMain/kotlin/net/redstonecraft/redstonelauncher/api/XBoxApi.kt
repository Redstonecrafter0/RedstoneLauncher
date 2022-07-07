package net.redstonecraft.redstonelauncher.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.redstonecraft.redstonelauncher.accounts.MicrosoftCredentials
import java.net.URL
import java.net.URLEncoder
import java.time.Instant

class XBoxApi(accessToken: String, var refreshToken: String, var validUntil: Long) {

    companion object {

        fun login(creds: MicrosoftCredentials): MicrosoftCredentials {
            val now = Instant.now().toEpochMilli() - 60000
            return if (creds.minecraftBearerValidUntil <= now) {
                val api = XBoxApi(creds.accessToken, creds.refreshToken, creds.validUntil)
                val xbl = api.getXBoxLiveToken().Token
                val xsts = api.getXSTSToken(xbl)
                val token = api.getMinecraftToken(xsts.Token, xsts.DisplayClaims.xui.first().uhs)
                MicrosoftCredentials(api.accessToken, api.refreshToken, api.validUntil, token.access_token, token.expires_in * 1000 + now)
            } else {
                creds
            }
        }
    }

    var accessToken: String = accessToken
        get() {
            if (validUntil <= Instant.now().toEpochMilli() - 60000) {
                val response = getMicrosoftTokenPair(refreshToken)
                field = response.access_token
                refreshToken = response.refresh_token
                validUntil = response.expires_in * 1000 + Instant.now().toEpochMilli()
            }
            return field
        }
        set(_) = error("Can't manually reassign accessToken.")

    fun getMicrosoftTokenPair(refreshToken: String): RefreshTokenResponse {
        return Json.decodeFromString(postRequest("https://login.live.com/oauth20_token.srf", mapOf("client_id" to "000000004C12AE6F", "refresh_token" to refreshToken, "grant_type" to "refresh_token")))
    }

    fun getXBoxLiveToken(): XBoxAuthResponse {
        return Json.decodeFromString(postRequest("https://user.auth.xboxlive.com/user/authenticate", Json.encodeToString(XBoxLiveAuthRequest(accessToken))))
    }

    fun getXSTSToken(accessToken: String): XBoxAuthResponse {
        return Json.decodeFromString(postRequest("https://xsts.auth.xboxlive.com/xsts/authorize", Json.encodeToString(XSTSRequest(accessToken))))
    }

    fun getMinecraftToken(accessToken: String, uhs: String): MinecraftAuthResponse {
        return Json.decodeFromString(postRequest("https://api.minecraftservices.com/authentication/login_with_xbox", Json.encodeToString(MinecraftAuthRequest(uhs, accessToken))))
    }

    private fun postRequest(url: String, data: String, contentType: String = "application/json"): String {
        val buffer = data.encodeToByteArray()
        val conn = URL(url).openConnection()
        conn.setRequestProperty("Content-Type", contentType)
        conn.setRequestProperty("Content-Length", buffer.size.toString())
        if (contentType == "application/json") {
            conn.setRequestProperty("Accept", "application/json")
        }
        conn.doInput = true
        conn.doOutput = true
        conn.outputStream.write(buffer)
        conn.outputStream.close()
        val response = conn.inputStream.readAllBytes().decodeToString()
        conn.inputStream.close()
        return response
    }

    private fun postRequest(url: String, params: Map<String, String>): String {
        return postRequest(url, params.map { (k, v) -> "${URLEncoder.encode(k, Charsets.UTF_8)}=${URLEncoder.encode(v, Charsets.UTF_8)}" }.joinToString("&"), "application/x-www-form-urlencoded")
    }

    @Serializable
    data class RefreshTokenResponse(val token_type: String, val scope: String, val expires_in: Long, val access_token: String, val refresh_token: String)

    @Serializable
    data class XBoxLiveAuthRequest(val Properties: PropertiesData, val RelyingParty: String, val TokenType: String) {

        constructor(accessToken: String) : this(
            PropertiesData("RPS", "user.auth.xboxlive.com", accessToken),
            "http://auth.xboxlive.com",
            "JWT"
        )

        @Serializable
        data class PropertiesData(val AuthMethod: String, val SiteName: String, val RpsTicket: String)
    }

    @Serializable
    data class XBoxAuthResponse(val IssueInstant: String, val NotAfter: String, val Token: String, val DisplayClaims: DisplayClaimsData) {

        @Serializable
        data class DisplayClaimsData(val xui: List<XUI>) {

            @Serializable
            data class XUI(val uhs: String)
        }
    }

    @Serializable
    data class XSTSRequest(val Properties: PropertiesData, val RelyingParty: String, val TokenType: String) {

        constructor(accessToken: String) : this(
            PropertiesData("RETAIL", listOf(accessToken)),
            "rp://api.minecraftservices.com/",
            "JWT"
        )

        @Serializable
        data class PropertiesData(val SandboxId: String, val UserTokens: List<String>)
    }

    @Serializable
    data class MinecraftAuthRequest(val identityToken: String, val ensureLegacyEnabled: Boolean) {

        constructor(uhs: String, accessToken: String) : this("XBL3.0 x=$uhs;$accessToken", true)
    }

    @Serializable
    data class MinecraftAuthResponse(val username: String, val roles: List<String>, val access_token: String, val token_type: String, val expires_in: Long)
}
