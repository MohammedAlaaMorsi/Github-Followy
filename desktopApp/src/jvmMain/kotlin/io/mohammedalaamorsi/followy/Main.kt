package io.mohammedalaamorsi.followy

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin
import io.mohammedalaamorsi.followy.shared.App
import io.mohammedalaamorsi.followy.shared.di.appModule

fun main() = application {
    startKoin {
        modules(appModule)
    }
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "GitHub Followy",
    ) {
        App()
    }
}
