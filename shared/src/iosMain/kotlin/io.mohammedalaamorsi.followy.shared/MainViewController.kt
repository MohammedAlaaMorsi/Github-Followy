package template.shared

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.dsl.module
import template.shared.data.local.SecureTokenStorage
import template.shared.data.local.DatabaseDriverFactory
import template.shared.data.local.IOSDatabaseDriverFactory
import template.shared.data.preferences.createIOSDataStore

val iosModule = module {
    single { createIOSDataStore() }
    single<DatabaseDriverFactory> { IOSDatabaseDriverFactory() }
    single { SecureTokenStorage().apply { setDataStore(get()) } }
}

@Suppress("ktlint:standard:function-naming", "FunctionNaming")
fun MainViewController() = ComposeUIViewController {
    startKoin {
        modules(
            appModule,
            iosModule
        )
    }
    App()
}
