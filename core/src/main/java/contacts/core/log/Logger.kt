package contacts.core.log

import android.util.Log

/**
 * Logs input/output messages from all APIs provided in this library that are accessible via a
 * [contacts.core.Contacts] instance.
 */
interface Logger {

    /**
     * True if messages should redact private user data to uphold privacy laws when logging in
     * production. Read more in [contacts.core.Redactable].
     */
    val redactMessages: Boolean

    /**
     * The message that should be logged. This message will be redacted if [redactMessages] is true.
     */
    fun log(message: String)

}

/**
 * No-op logger. Does nothing.
 */
class EmptyLogger : Logger {
    override val redactMessages: Boolean = false
    override fun log(message: String) {}
}

/**
 * A [Logger] that uses [android.util.Log.d] using the given [tag].
 */
class AndroidLogger @JvmOverloads constructor(
    override val redactMessages: Boolean = true,
    private val tag: String = TAG
) : Logger {

    override fun log(message: String) {
        Log.d(tag, message)
    }

    companion object {
        private const val TAG = "ContactsDebug"
    }
}