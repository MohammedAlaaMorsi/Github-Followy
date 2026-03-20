package io.mohammedalaamorsi.followy.shared.di

import io.mohammedalaamorsi.followy.shared.data.local.DatabaseDriverFactory
import io.mohammedalaamorsi.followy.shared.data.local.DesktopDatabaseDriverFactory
import io.mohammedalaamorsi.followy.shared.data.preferences.createDesktopDataStore
import org.koin.dsl.module

/**
 * Koin module for desktop-specific dependencies.
 */
val desktopModule = module {
    single { createDesktopDataStore() }
    single<DatabaseDriverFactory> { DesktopDatabaseDriverFactory() }
}
