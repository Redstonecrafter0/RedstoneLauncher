package net.redstonecraft.redstonelauncher.utils

fun String.toLongUUID() = replace("-", "").replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})".toRegex(), "$1-$2-$3-$4-$5")
