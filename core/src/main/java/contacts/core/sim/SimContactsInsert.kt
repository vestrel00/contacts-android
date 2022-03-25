package contacts.core.sim

import android.content.ContentResolver
import android.util.Log
import contacts.core.*
import contacts.core.entities.NewSimContact
import contacts.core.entities.operation.SimContactsOperation
import contacts.core.entities.table.Table
import contacts.core.util.unsafeLazy

// TODO Calculate max character limits for name and number. Pre-emptively fail the insert for
// entries that breach the limit. Make sure to update documentation.

/**
 * Inserts one or more user SIM contacts into the SIM contacts table.
 *
 * ## Blank SIM contacts are ignored
 *
 * Blank SimContacts (name AND number are both null or blank) will NOT be inserted. The name OR
 * number can be null or blank but not both.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All inserts will do nothing if this permission is not granted.
 *
 * ## Usage
 *
 * To insert a contact to the SIM card,
 *
 * ```kotlin
 * val result = insert
 *      .simContact(NewSimContact(name = "Dude", number = "5555555555"))
 *      .commit()
 * ```
 */
interface SimContactsInsert : CrudApi {

    /**
     * Adds a new [NewSimContact] to the insert queue, which will be inserted on [commit].
     * The new instance is configured by the [configureSimContact] function.
     */
    fun simContact(configureSimContact: NewSimContact.() -> Unit): SimContactsInsert

    /**
     * Adds the given [simContacts] to the insert queue, which will be inserted on [commit].
     * Duplicates (SIM contacts with identical attributes to already added SIM contacts) are
     * ignored.
     */
    fun simContacts(vararg simContacts: NewSimContact): SimContactsInsert

    /**
     * See [SimContactsInsert.simContacts].
     */
    fun simContacts(simContacts: Collection<NewSimContact>): SimContactsInsert

    /**
     * See [SimContactsInsert.simContacts].
     */
    fun simContacts(simContacts: Sequence<NewSimContact>): SimContactsInsert

    /**
     * Inserts the [NewSimContact]s in the queue (added via [simContacts]) and returns the
     * [Result].
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
     * Inserts the [NewSimContact]s in the queue (added via [simContacts]) and returns the
     * [Result].
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
     * **Cancelling does not undo insertions. This means that depending on when the cancellation
     * occurs, some if not all of the SimContacts in the insert queue may have already been
     * inserted.**
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
    override fun redactedCopy(): SimContactsInsert

    interface Result : CrudApi.Result {

        /**
         * True if all NewSimContacts have successfully been inserted. False if even one insert
         * failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [simContact] has been successfully inserted. False otherwise.
         */
        fun isSuccessful(simContact: NewSimContact): Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun SimContactsInsert(contacts: Contacts): SimContactsInsert =
    SimContactsInsertImpl(contacts)

private class SimContactsInsertImpl(
    override val contactsApi: Contacts,

    private val simContacts: MutableSet<NewSimContact> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : SimContactsInsert {

    override fun toString(): String =
        """
            SimContactsInsert {
                simContacts: $simContacts
                hasPermission: ${permissions.canInsertToSim()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsInsert = SimContactsInsertImpl(
        contactsApi,

        // Redact SIM contact data.
        simContacts.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun simContact(configureSimContact: NewSimContact.() -> Unit) =
        simContacts(NewSimContact().apply(configureSimContact))

    override fun simContacts(vararg simContacts: NewSimContact) =
        simContacts(simContacts.asSequence())

    override fun simContacts(simContacts: Collection<NewSimContact>) =
        simContacts(simContacts.asSequence())

    override fun simContacts(simContacts: Sequence<NewSimContact>): SimContactsInsert =
        apply {
            this.simContacts.addAll(simContacts.redactedCopiesOrThis(isRedacted))
        }

    override fun commit(): SimContactsInsert.Result = commit { false }

    override fun commit(cancel: () -> Boolean): SimContactsInsert.Result {
        onPreExecute()

        return if (simContacts.isEmpty() || !permissions.canInsertToSim() || cancel()) {
            SimContactsInsertFailed()
        } else {

            val results = mutableMapOf<NewSimContact, Boolean>()

            for (simContact in simContacts) {
                if (cancel()) {
                    break
                }

                results[simContact] =
                    !simContact.isBlank && contentResolver.insertSimContact(simContact)
            }
            SimContactsInsertResult(results)
        }
            .redactedCopyOrThis(isRedacted)
            .apply { onPostExecute(contactsApi) }
    }
}

private fun ContentResolver.insertSimContact(simContact: NewSimContact): Boolean {
    val result = SimContactsOperation().insert(simContact)?.let {
        insert(Table.SimContacts.uri, it)
    }

    // FIXME If result is not null, make sure a new row is inserted in the SIM table. In some OEMs,
    // the result will be successful but no new row is added to the SIM table.
    // Successful result is always "content://icc/adn/0"
    return result != null
}

private class SimContactsInsertResult private constructor(
    private val simContactsMap: Map<NewSimContact, Boolean>,
    override val isRedacted: Boolean
) : SimContactsInsert.Result {

    constructor(simContactsMap: Map<NewSimContact, Boolean>) : this(simContactsMap, false)

    override fun toString(): String =
        """
            SimContactsInsert.Result {
                isSuccessful: $isSuccessful
                simContactsMap: $simContactsMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsInsert.Result = SimContactsInsertResult(
        simContactsMap.redactedKeys(),
        isRedacted = true
    )

    override val isSuccessful: Boolean by unsafeLazy {
        // By default, all returns true when the collection is empty. So, we override that.
        simContactsMap.run { isNotEmpty() && all { it.value } }
    }

    override fun isSuccessful(simContact: NewSimContact): Boolean =
        simContactsMap[simContact] == true
}

private class SimContactsInsertFailed private constructor(override val isRedacted: Boolean) :
    SimContactsInsert.Result {

    constructor() : this(false)

    override fun toString(): String =
        """
            SimContactsInsert.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsInsert.Result = SimContactsInsertFailed(true)

    override val isSuccessful: Boolean = false

    override fun isSuccessful(simContact: NewSimContact): Boolean = false
}