package net.redstonecraft.redstonelauncher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.util.concurrent.Executors

object RedstoneLauncher {

    val updateDownloadIndicatorState = mutableStateOf(true)

    var updateDownloadIndicator by updateDownloadIndicatorState
        private set

    val runningDownloads = mutableMapOf<String, Double>()
        get() {
            updateDownloadIndicator = true
            return field
        }

    var openCallback: (() -> Unit) = {}
    var githubAuthCallback: ((Map<String, String>) -> Unit) = {}

    var authenticatingResponse = Config::class.java.getResourceAsStream("/html/authenticating.html")!!.readAllBytes().decodeToString()

    val webServer = HttpServer.create(InetSocketAddress("127.0.0.1", 4561), 0).apply {
        executor = Executors.newSingleThreadExecutor()
        createContext("/api/auth/github") {
            githubAuthCallback(
                it.requestURI.rawQuery.split("&").associate { i ->
                    i.split("=", limit = 2)
                        .map { j -> URLDecoder.decode(j, Charsets.UTF_8) }
                        .let { j -> j[0] to if (j.size == 2) j[1] else "" }
                })
            it.responseHeaders["Location"] = "http://localhost:4561/authenticating"
            it.sendResponseHeaders(302, 0)
        }
        createContext("/api/open") {
            openCallback()
        }
        createContext("/authenticating") {
            it.sendResponseHeaders(200, authenticatingResponse.length.toLong())
            val os = it.responseBody
            os.write(authenticatingResponse.encodeToByteArray())
            os.close()
        }
        start()
    }
}
