package template.shared

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.context.startKoin
import template.shared.di.appModule

@Suppress("ktlint:standard:function-naming", "FunctionNaming")
fun MainViewController() = ComposeUIViewController {
    startKoin {
        modules(appModule)
    }
    App()
}
