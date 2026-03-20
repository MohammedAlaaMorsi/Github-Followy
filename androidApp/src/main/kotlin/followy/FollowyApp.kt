package followy

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.android.ext.android.get
import io.mohammedalaamorsi.followy.shared.data.local.AuthTokenStorage
import io.mohammedalaamorsi.followy.shared.data.local.SecureTokenStorage
import io.mohammedalaamorsi.followy.shared.di.appModule
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubAuthConfigProvider
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubAuthConfig
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthConfig
import io.mohammedalaamorsi.followy.shared.Platform
import io.mohammedalaamorsi.followy.shared.AndroidPlatform
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthHandler

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
        
        // Initialize AuthTokenStorage from Koin
        AuthTokenStorage.initialize(get())
    }
}

val androidModule = module {
    single<DataStore<Preferences>> { 
        (androidContext().applicationContext as TemplateApp).dataStore
    }
    
    // Platform and OAuth integration using androidContext
    single<Platform> { AndroidPlatform(androidContext()) }
    factory { GitHubOAuthHandler(androidContext()) }
    
    single { 
        SecureTokenStorage().apply {
            setDataStore(get())
            setContext(androidContext())
        }
    }
    
    single<GitHubAuthConfigProvider> { 
        GitHubAuthConfig(
            clientId = GitHubOAuthConfig.clientId,
            clientSecret = GitHubOAuthConfig.clientSecret,
            redirectUri = GitHubOAuthConfig.redirectUri
        )
    }
}
