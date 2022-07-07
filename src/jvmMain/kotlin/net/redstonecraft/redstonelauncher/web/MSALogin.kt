package net.redstonecraft.redstonelauncher.web

import net.redstonecraft.redstonelauncher.Config
import org.cef.CefApp
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefDisplayHandlerAdapter
import java.awt.Desktop
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.net.URL
import java.net.URLDecoder
import java.time.Instant
import javax.imageio.ImageIO
import javax.swing.JFrame

class MSALogin(private val callback: (accessToken: String, refreshToken: String, validUntil: Long) -> Unit): JFrame("RedstoneLauncher MSA Login") {

    companion object {

        init {
            CefApp.startup(emptyArray())
        }

        val app = CefApp.getInstance(CefSettings().apply {
            windowless_rendering_enabled = false
        })
    }

    val client = app.createClient()
    var browser = client.createBrowser("https://login.live.com/oauth20_authorize.srf?client_id=000000004C12AE6F&redirect_uri=https://login.live.com/oauth20_desktop.srf&scope=service::user.auth.xboxlive.com::MBI_SSL&display=touch&response_type=token", false, false)

    init {
        client.removeDisplayHandler()
        client.addDisplayHandler(object : CefDisplayHandlerAdapter() {
            override fun onAddressChange(browser: CefBrowser, frame: CefFrame, url: String) {
                if (url.startsWith("https://login.live.com/")) {
                    if (url.startsWith("https://login.live.com/oauth20_desktop.srf?lc=")) {
                        val data = url.split("#", limit = 2)[1].split("&").associate { it.split("=", limit = 2).let { i -> i[0] to URLDecoder.decode(i[1], Charsets.UTF_8) } }
                        if ("error" !in data) {
                            val accessToken = data["access_token"]!!
                            val validUntil = data["expires_in"]!!.toLong() * 1000 + Instant.now().toEpochMilli()
                            val refreshToken = data["refresh_token"]!!
                            callback(accessToken, refreshToken, validUntil)
                        }
                        isVisible = false
                        browser.close(false)
                    }
                } else {
                    if (url.startsWith("http:") || url.startsWith("https:")) {
                        Desktop.getDesktop().browse(URL(url).toURI())
                    }
                    isVisible = false
                    browser.close(false)
                }
            }
        })
        isUndecorated = true
        iconImage = ImageIO.read(Config::class.java.getResourceAsStream("/icon.png"))
        contentPane.add(browser.uiComponent)
        defaultCloseOperation = HIDE_ON_CLOSE
        pack()
        size = Dimension(1280, 720)
        isVisible = true
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                client.dispose()
                dispose()
            }
        })
        setLocationRelativeTo(null)
    }
}
