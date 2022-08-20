package net.redstonecraft.redstonelauncher.web

import java.io.File
import java.net.URLDecoder
import java.time.Instant
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MSALogin(private val callback: (accessToken: String, refreshToken: String, validUntil: Long) -> Unit) {

    companion object {
        val threadPool: ExecutorService = Executors.newCachedThreadPool()
    }

    init {
        threadPool.submit {
            val process = ProcessBuilder(File(System.getProperty("compose.application.resources.dir")).listFiles { _, name -> name.startsWith("RedstoneLauncher-MSA-Login") }!![0].resolve("RedstoneLauncher-MSA-Login").canonicalPath).start()
            val url = process.inputStream.bufferedReader().readLine()
            val data = url.split("#", limit = 2)[1].split("&").associate { it.split("=", limit = 2).let { i -> i[0] to URLDecoder.decode(i[1], Charsets.UTF_8) } }
            if ("error" !in data) {
                val accessToken = data["access_token"]!!
                val validUntil = data["expires_in"]!!.toLong() * 1000 + Instant.now().toEpochMilli()
                val refreshToken = data["refresh_token"]!!
                callback(accessToken, refreshToken, validUntil)
            }
        }
    }
}
