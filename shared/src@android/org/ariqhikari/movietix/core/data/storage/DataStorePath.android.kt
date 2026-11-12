package org.ariqhikari.movietix.core.data.storage

import android.content.Context
import java.io.File

/**
 * Android context provider singleton for shared module storage path.
 * Initialized from Android Application/Activity.
 */
object AndroidContextProvider {
    private var contextRef: Context? = null

    fun init(context: Context) {
        contextRef = context.applicationContext
    }

    val context: Context
        get() = contextRef ?: error("AndroidContextProvider must be initialized in MainActivity/Application")
}

actual fun createDataStorePath(fileName: String): String {
    return File(AndroidContextProvider.context.filesDir, fileName).absolutePath
}
