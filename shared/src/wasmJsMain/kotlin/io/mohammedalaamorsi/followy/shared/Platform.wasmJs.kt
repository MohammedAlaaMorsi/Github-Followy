package io.mohammedalaamorsi.followy.shared

class WasmPlatform: Platform {
    override val name: String = "Web (Wasm)"
}

actual fun getPlatform(): Platform = WasmPlatform()
