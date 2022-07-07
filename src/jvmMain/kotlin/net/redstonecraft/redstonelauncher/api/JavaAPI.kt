package net.redstonecraft.redstonelauncher.api

import java.net.URL

interface JavaAPI {

    fun getVersions(): List<Int>
    fun getVersion(version: Int, jdk: Boolean): JavaPackage

    fun request(url: String): String {
        val conn = URL(url).openConnection()
        conn.setRequestProperty("Accept", "application/json")
        return conn.inputStream.readAllBytes().decodeToString()
    }
}

enum class OS {
    WINDOWS, LINUX;

    companion object {
        val current = run {
            val os = System.getProperty("os.name")
            when {
                "win" in os -> WINDOWS
                "linux" in os -> LINUX
                else -> error("Unsupported OS")
            }
        }
    }
}

data class JavaPackage(
    val filename: String,
    val url: String,
    val version: List<Int>,
    val hash: String? = null,
    val hashAlgorithm: String? = null
)
