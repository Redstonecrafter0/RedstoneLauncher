package net.redstonecraft.redstonelauncher.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URL

object GitHubApi {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun getCurrentUser(token: String): GitHubApiUser {
        return json.decodeFromString(request("https://api.github.com/user", token))
    }

    private fun request(url: String, token: String, accept: String = "application/vnd.github+json"): String {
        val req = URL(url).openConnection()
        req.setRequestProperty("Authorization", "bearer $token")
        req.setRequestProperty("Accept", accept)
        return req.getInputStream().readAllBytes().decodeToString()
    }
}

@Serializable
data class GitHubApiUser(val login: String)
