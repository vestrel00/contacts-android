package contacts.core.sim

import contacts.core.*
import contacts.core.entities.ExistingSimContactEntity
import contacts.core.entities.operation.SimContactsOperation
import contacts.core.entities.table.Table
import contacts.core.util.unsafeLazy

/**
 * Deletes one or more SIM contacts from the SIM Contacts table.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All inserts will do nothing if this permission is not granted.
 *
 * ## Usage
 *
 * To delete the given simContacts,
 *
 * ```kotlin
 * simContactsDelete
 *      .simContacts(simContacts)
 *      .commit()
 * ```
 */
interface SimContactsDelete : CrudApi {

    /**
     * Adds the given [simContacts] to the delete queue, which will be deleted on [commit].
     */
    fun simContacts(vararg simContacts: ExistingSimContactEntity): SimContactsDelete

    /**
     * See [SimContactsDelete.simContacts].
     */
    fun simContacts(simContacts: Collection<ExistingSimContactEntity>): SimContactsDelete

    /**
     * See [SimContactsDelete.simContacts].
     */
    fun simContacts(simContacts: Sequence<ExistingSimContactEntity>): SimContactsDelete

    /**
     * Deletes the [ExistingSimContactEntity]s in the queue (added via [simContacts]) and returns
     * the [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

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
    override fun redactedCopy(): SimContactsDelete

    interface Result : CrudApi.Result {

        /**
         * True if all SimContacts have successfully been deleted. False if even one delete
         * failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [simContact] has been successfully deleted. False otherwise.
         */
        fun isSuccessful(simContact: ExistingSimContactEntity): Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun SimContactsDelete(contacts: Contacts): SimContactsDelete =
    SimContactsDeleteImpl(contacts)

private class SimContactsDeleteImpl(
    override val contactsApi: Contacts,

    private val simContacts: MutableSet<ExistingSimContactEntity> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : SimContactsDelete {

    override fun toString(): String =
        """
            SimContactsDelete {
                simContacts: $simContacts
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsDelete = SimContactsDeleteImpl(
        contactsApi,

        // Redact SIM contact data.
        simContacts.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun simContacts(vararg simContacts: ExistingSimContactEntity) =
        simContacts(simContacts.asSequence())

    override fun simContacts(simContacts: Collection<ExistingSimContactEntity>) =
        simContacts(simContacts.asSequence())

    override fun simContacts(simContacts: Sequence<ExistingSimContactEntity>): SimContactsDelete =
        apply {
            this.simContacts.addAll(simContacts.redactedCopiesOrThis(isRedacted))
        }

    override fun commit(): SimContactsDelete.Result {
        onPreExecute()

        return if (simContacts.isEmpty() || !permissions.canUpdateDelete()) {
            SimContactsDeleteResult(emptyMap())
        } else {
            val results = mutableMapOf<Long, Boolean>()
            for (simContact in simContacts) {
                val result = contentResolver.delete(
                    Table.SimContacts.uri,
                    SimContactsOperation().delete(simContact),
                    null
                )
                results[simContact.id] = result > 0
            }
            SimContactsDeleteResult(results)
        }
            .redactedCopyOrThis(isRedacted)
            .apply { onPostExecute(contactsApi) }
    }
}

private class SimContactsDeleteResult private constructor(
    private val simContactIdsResultMap: Map<Long, Boolean>,
    override val isRedacted: Boolean
) : SimContactsDelete.Result {

    constructor(simContactIdsResultMap: Map<Long, Boolean>) : this(
        simContactIdsResultMap,
        false
    )

    override fun toString(): String =
        """
            SimContactsDelete.Result {
                isSuccessful: $isSuccessful
                simContactIdsResultMap: $simContactIdsResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsDelete.Result = SimContactsDeleteResult(
        simContactIdsResultMap, true
    )

    override val isSuccessful: Boolean by unsafeLazy {
        // By default, all returns true when the collection is empty. So, we override that.
        simContactIdsResultMap.run { isNotEmpty() && all { it.value } }
    }

    override fun isSuccessful(simContact: ExistingSimContactEntity): Boolean {
        return simContactIdsResultMap.getOrElse(simContact.id) { false }
    }
}