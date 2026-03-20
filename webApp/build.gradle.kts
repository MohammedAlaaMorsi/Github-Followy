import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static(project.projectDir.path + "/src/wasmJsMain/resources")
                    port = 8080
                    proxy = mutableListOf(KotlinWebpackConfig.DevServer.Proxy(
                        mutableListOf("/github-oauth"),
                        "https://github.com",
                        mutableMapOf("^/github-oauth" to ""),
                        true,
                        true
                    ))
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
            }
        }
    }
}
