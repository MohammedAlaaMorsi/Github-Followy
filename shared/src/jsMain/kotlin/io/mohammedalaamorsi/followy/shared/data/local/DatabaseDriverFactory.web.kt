package io.mohammedalaamorsi.followy.shared.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * Web implementation of DatabaseDriverFactory.
 * Currently returns null as SqlDelight on Web requires additional JS worker setup.
 */
class WebDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver? {
        return null
    }
}
