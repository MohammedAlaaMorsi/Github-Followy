package io.mohammedalaamorsi.followy.shared.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

/**
 * Desktop implementation of [DatabaseDriverFactory] using [JdbcSqliteDriver].
 */
class DesktopDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver? {
        // TODO: Implement database when needed
        // val databaseFile = File(System.getProperty("user.home"), ".github_followy.db")
        // val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY + databaseFile.absolutePath)
        // try {
        //     AppDatabase.Schema.create(driver)
        // } catch (e: Exception) {
        // }
        // return driver
        return null
    }
}
