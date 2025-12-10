package followy

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import io.mohammedalaamorsi.followy.shared.data.local.AuthTokenStorage
import io.mohammedalaamorsi.followy.shared.data.local.SecureTokenStorage
import io.mohammedalaamorsi.followy.shared.di.appModule

// DataStore delegate
private val Application.dataStore: DataStore<Preferences> by preferencesDataStore(name = "github_followy_prefs")

class TemplateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@TemplateApp)
            modules(
                appModule,
                androidModule
            )
        }
        
        // Initialize SecureTokenStorage with context and DataStore
        val storage = SecureTokenStorage(dataStore).apply {
            setContext(this@TemplateApp)
        }
        AuthTokenStorage.initialize(storage)
    }
}

val androidModule = module {
    single<DataStore<Preferences>> { 
        (androidContext().applicationContext as TemplateApp).dataStore
    }
}
