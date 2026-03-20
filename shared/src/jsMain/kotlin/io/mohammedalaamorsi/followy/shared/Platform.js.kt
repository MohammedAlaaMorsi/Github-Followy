package io.mohammedalaamorsi.followy.shared

class JsPlatform: Platform {
    override val name: String = "Web (JS)"
}

actual fun getPlatform(): Platform = JsPlatform()
