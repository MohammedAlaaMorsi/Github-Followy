package io.mohammedalaamorsi.followy.shared.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/**
 * Platform agnostic function to create a [DataStore] instance.
 *
 * Each platform will call this and provide their own [producePath] lambda
 * to look up the preference file based on the platform's standards.
 */
fun createDataStore(
    producePath: () -> String,
): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() },
    )
}

/**
 * NOTE FOR TEMPLATE USERS:
 * Update the name of the preference file after cloning this template.
 */
const val DATA_STORE_FILE_NAME = "TODO.preferences_pb"
