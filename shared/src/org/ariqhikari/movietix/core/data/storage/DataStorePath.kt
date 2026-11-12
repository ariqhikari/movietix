package org.ariqhikari.movietix.core.data.storage

/**
 * Platform-specific file path generator for DataStore files.
 * Android: uses context.filesDir
 * iOS: uses NSDocumentDirectory
 */
expect fun createDataStorePath(fileName: String): String
