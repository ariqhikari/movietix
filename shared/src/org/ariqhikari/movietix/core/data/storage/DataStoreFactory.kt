package org.ariqhikari.movietix.core.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/**
 * Creates a DataStore<Preferences> instance targeting the platform storage path.
 */
fun createDataStore(fileName: String = "movietix.preferences_pb"): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { createDataStorePath(fileName).toPath() }
    )
}
