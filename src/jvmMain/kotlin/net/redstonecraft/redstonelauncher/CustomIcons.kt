package net.redstonecraft.redstonelauncher

object CustomIcons {

    val fallbackProfile by lazy { javaClass.getResourceAsStream("/defaultProfile.png")?.readAllBytes()!! }
    val minecraft by lazy { javaClass.getResourceAsStream("/icons/minecraft.png")!!.readAllBytes() }
}
