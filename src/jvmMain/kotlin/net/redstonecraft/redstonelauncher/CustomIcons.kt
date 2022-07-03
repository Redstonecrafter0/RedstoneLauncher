package net.redstonecraft.redstonelauncher

import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.unit.Density
import org.xml.sax.InputSource

object CustomIcons {

    val videoGame = load("/icons/baseline_videogame_asset_24.xml")
    val code = load("/icons/baseline_code_24.xml")
    val settings = load("/icons/baseline_settings_24.xml")
    val accountSettings = load("/icons/baseline_manage_accounts_24.xml")

    val fallbackProfile by lazy { javaClass.getResourceAsStream("/defaultProfile.png")?.readAllBytes()!! }

    private fun load(location: String) = loadXmlImageVector(InputSource(javaClass.getResourceAsStream(location)), Density(1F))

}
