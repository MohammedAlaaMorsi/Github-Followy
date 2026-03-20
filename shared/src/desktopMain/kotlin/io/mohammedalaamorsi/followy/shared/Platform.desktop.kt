package io.mohammedalaamorsi.followy.shared

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val context: Any? = null
}
