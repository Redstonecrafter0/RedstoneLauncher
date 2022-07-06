package net.redstonecraft.redstonelauncher.api

class XBoxApi(accessToken: String, private val refreshToken: String, private val validUntil: Long) {

    private var accessToken: String = accessToken
        get() {
            field = ""
            return field
        }
        set(value) = error("Can't manually reassign accessToken.")

}
