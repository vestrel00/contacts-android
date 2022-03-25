package contacts.core.sim

import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.MutableSimContact
import contacts.core.entities.SimContact
import contacts.core.entities.operation.SimContactsOperation
import contacts.core.entities.table.Table
import contacts.core.util.unsafeLazy

// TODO Calculate max character limits for name and number. Pre-emptively fail the update for
// entries that breach the limit. Make sure to update documentation.

/**
 * Updates one or more user SIM contacts in the SIM contacts table.
 *
 * ## Blank SIM contacts are ignored
 *
 * Blank SimContacts (name AND number are both null or blank) will NOT be updated. The name OR
 * number can be null or blank but not both.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All updates will do nothing if this permission is not granted.
 *
 * ## Usage
 *
 * To update a contact to the SIM card,
 *
 * ```kotlin
 * val result = update
 *      .simContact(simContact, simContact.mutableCopy { ... })
 *      .commit()
 * ```
 */
interface SimContactsUpdate : CrudApi {

    /**
     * Adds an [Entry] in the update queue with the given [original] and [updated] contacts.
     */
    fun simContact(original: SimContact, updated: MutableSimContact): SimContactsUpdate

    /**
     * Adds the given [entries] to the update queue, which will be updated on [commit].
     * Duplicates (SIM contacts with identical attributes to already added SIM contacts) are
     * ignored.
     */
    fun simContacts(vararg entries: Entry): SimContactsUpdate

    /**
     * See [SimContactsUpdate.simContacts].
     */
    fun simContacts(entries: Collection<Entry>): SimContactsUpdate

    /**
     * See [SimContactsUpdate.simContacts].
     */
    fun simContacts(entries: Sequence<Entry>): SimContactsUpdate

    /**
     * Updates the [Entry]s in the queue (added via [simContacts]) and returns the [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION]. This will do nothing if this permission is
     * not granted.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    /**
     * Updates the [Entry]s in the queue (added via [simContacts]) and returns the [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION]. This will do nothing if this permission is
     * not granted.
     *
     * ## Cancellation
     *
     * To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * **Cancelling does not undo updates. This means that depending on when the cancellation
     * occurs, some if not all of the SimContacts in the update queue may have already been
     * updated.**
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun commit(cancel: () -> Boolean = { false }): Result
    fun commit(cancel: () -> Boolean): Result

    /**
     * Returns a redacted instance where all private user data are redacted.
     *
     * ## Redacted instances may produce invalid results!
     *
     * Redacted instance may have critical information redacted, which is required to make
     * the operation work properly.
     *
     * **Redacted operations should typically only be used for logging in production!**
     */
    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): SimContactsUpdate

    // We could use a Pair but it's too generic. We have more control this way.
    data class Entry internal constructor(
        val original: SimContact,
        val updated: MutableSimContact,

        override val isRedacted: Boolean
    ) : Redactable {

        constructor(original: SimContact, updated: MutableSimContact) :
                this(original, updated, false)

        override fun redactedCopy() = copy(
            original = original.redactedCopy(),
            updated = updated.redactedCopy(),

            isRedacted = true
        )
    }

    interface Result : CrudApi.Result {

        /**
         * True if all Entries have successfully been updated. False if even one update failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the SIM contact with the given [simContactId] has been successfully updated.
         * False otherwise.
         */
        fun isSuccessful(simContactId: Long): Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun SimContactsUpdate(contacts: Contacts): SimContactsUpdate =
    SimContactsUpdateImpl(contacts)

private class SimContactsUpdateImpl(
    override val contactsApi: Contacts,

    private val entries: MutableSet<SimContactsUpdate.Entry> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : SimContactsUpdate {

    override fun toString(): String =
        """
            SimContactsUpdate {
                entries: $entries
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsUpdate = SimContactsUpdateImpl(
        contactsApi,

        // Redact SIM contact data.
        entries.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun simContact(original: SimContact, updated: MutableSimContact) =
        simContacts(SimContactsUpdate.Entry(original, updated))

    override fun simContacts(vararg entries: SimContactsUpdate.Entry) =
        simContacts(entries.asSequence())

    override fun simContacts(entries: Collection<SimContactsUpdate.Entry>) =
        simContacts(entries.asSequence())

    override fun simContacts(entries: Sequence<SimContactsUpdate.Entry>): SimContactsUpdate =
        apply {
            this.entries.addAll(entries.redactedCopiesOrThis(isRedacted))
        }

    override fun commit(): SimContactsUpdate.Result = commit { false }

    override fun commit(cancel: () -> Boolean): SimContactsUpdate.Result {
        onPreExecute()

        return if (entries.isEmpty() || !permissions.canUpdateDelete() || cancel()) {
            SimContactsUpdateFailed()
        } else {

            val results = mutableMapOf<Long, Boolean>()

            for (entry in entries) {
                if (cancel()) {
                    break
                }

                results[entry.original.id] =
                    !entry.updated.isBlank && contentResolver.updateSimContact(
                        entry.original,
                        entry.updated
                    )
            }
            SimContactsUpdateResult(results)
        }
            .redactedCopyOrThis(isRedacted)
            .apply { onPostExecute(contactsApi) }
    }
}

private fun ContentResolver.updateSimContact(
    original: SimContact, updated: MutableSimContact
): Boolean {
    val result = SimContactsOperation().update(original, updated)?.let {
        update(Table.SimContacts.uri, it, null, null)
    }

    return result != null && result > 0
}

private class SimContactsUpdateResult private constructor(
    private val simContactIdsResultMap: Map<Long, Boolean>,
    override val isRedacted: Boolean
) : SimContactsUpdate.Result {

    constructor(simContactIdsResultMap: Map<Long, Boolean>) : this(simContactIdsResultMap, false)

    override fun toString(): String =
        """
            SimContactsUpdate.Result {
                isSuccessful: $isSuccessful
                simContactIdsResultMap: $simContactIdsResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsUpdate.Result = SimContactsUpdateResult(
        simContactIdsResultMap,
        isRedacted = true
    )

    override val isSuccessful: Boolean by unsafeLazy {
        // By default, all returns true when the collection is empty. So, we override that.
        simContactIdsResultMap.run { isNotEmpty() && all { it.value } }
    }

    override fun isSuccessful(simContactId: Long): Boolean =
        simContactIdsResultMap.getOrElse(simContactId) { false }
}

private class SimContactsUpdateFailed private constructor(override val isRedacted: Boolean) :
    SimContactsUpdate.Result {

    constructor() : this(false)

    override fun toString(): String =
        """
            SimContactsUpdate.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsUpdate.Result = SimContactsUpdateFailed(true)

    override val isSuccessful: Boolean = false

    override fun isSuccessful(simContactId: Long): Boolean = isSuccessful
}