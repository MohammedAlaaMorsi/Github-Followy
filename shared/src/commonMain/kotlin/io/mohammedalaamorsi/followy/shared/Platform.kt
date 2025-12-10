package io.mohammedalaamorsi.followy.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
