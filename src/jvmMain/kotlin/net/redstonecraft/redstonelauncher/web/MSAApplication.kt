package net.redstonecraft.redstonelauncher.web

//import org.cef.browser.CefBrowser
//import org.cef.browser.CefFrame
//import org.cef.handler.CefLoadHandlerAdapter
//import org.cef.network.CefRequest
import javax.swing.JFrame

class MSAApplication(private val url: String): JFrame("RedstoneLauncher MSA Login") {

    companion object {

        var callback: (accessToken: String, refreshToken: String) -> Unit = { _, _ ->  }

        fun launch(url: String) {
            MSAApplication(url)
        }
    }

//    lateinit var pandomium: Pandomium
//    lateinit var client: PandomiumClient
//    lateinit var browser: CefBrowser

    override fun frameInit() {
//        pandomium = Pandomium.buildDefault()
//        client = pandomium.createClient()
//        browser = client.loadURL(url)
        super.frameInit()
//        client.cefClient.removeLoadHandler()
//        client.cefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
//            override fun onLoadStart(p0: CefBrowser, p1: CefFrame?, p2: CefRequest.TransitionType?) {
//                println(p0.url)
//            }
//        })
//        contentPane.add(browser.uiComponent)
        isVisible = true
    }
}
