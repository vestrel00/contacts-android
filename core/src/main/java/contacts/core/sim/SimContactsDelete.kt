package contacts.core.sim

import contacts.core.*
import contacts.core.entities.ExistingSimContactEntity
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
 * In Kotlin,
 *
 * ```kotlin
 * val result = simContactsDelete.simContacts(simContacts).commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * SimContactsDelete.Result result = simContactsDelete.simContacts(simContacts).commit();
 * ```
 */
interface SimContactsDelete : CrudApi {

    /**
     * Adds the SIM contact with the given [name] and [number] to the delete queue, which will be
     * deleted on [commit].
     */
    fun simContact(name: String?, number: String?): SimContactsDelete

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

        /**
         * True if the SIM contact with the given [name] and [number] has been successfully deleted.
         * False otherwise.
         */
        fun isSuccessful(name: String?, number: String?): Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun SimContactsDelete(contacts: Contacts): SimContactsDelete =
    SimContactsDeleteImpl(contacts)

private class SimContactsDeleteImpl(
    override val contactsApi: Contacts,

    private val simContactsToDelete: MutableSet<SimContactToDelete> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : SimContactsDelete {

    override fun toString(): String =
        """
            SimContactsDelete {
                simContactsToDelete: $simContactsToDelete
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsDelete = SimContactsDeleteImpl(
        contactsApi,

        // Redact SIM contact data.
        simContactsToDelete.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun simContact(name: String?, number: String?): SimContactsDelete = apply {
        simContactsToDelete.add(
            SimContactToDelete(
                name = name,
                number = number
            ).redactedCopyOrThis(isRedacted)
        )
    }

    override fun simContacts(vararg simContacts: ExistingSimContactEntity) =
        simContacts(simContacts.asSequence())

    override fun simContacts(simContacts: Collection<ExistingSimContactEntity>) =
        simContacts(simContacts.asSequence())

    override fun simContacts(simContacts: Sequence<ExistingSimContactEntity>): SimContactsDelete =
        apply {
            this.simContactsToDelete.addAll(
                simContacts
                    .map { it.toSimContactToDelete() }
                    .redactedCopiesOrThis(isRedacted)
            )
        }

    override fun commit(): SimContactsDelete.Result {
        onPreExecute()

        return if (simContactsToDelete.isEmpty() || !permissions.canUpdateDelete()) {
            SimContactsDeleteResult(emptyMap())
        } else {
            val results = mutableMapOf<SimContactToDelete, Boolean>()
            for (simContactToDelete in simContactsToDelete) {
                val result = contentResolver.delete(
                    Table.SimContacts.uri,
                    simContactToDelete.deleteWhere,
                    null
                )
                results[simContactToDelete] = result > 0
            }
            SimContactsDeleteResult(results)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

private class SimContactsDeleteResult private constructor(
    private val simContactsToDeleteResultMap: Map<SimContactToDelete, Boolean>,
    override val isRedacted: Boolean
) : SimContactsDelete.Result {

    constructor(simContactIdsResultMap: Map<SimContactToDelete, Boolean>) : this(
        simContactIdsResultMap,
        false
    )

    override fun toString(): String =
        """
            SimContactsDelete.Result {
                isSuccessful: $isSuccessful
                simContactsToDeleteResultMap: $simContactsToDeleteResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsDelete.Result = SimContactsDeleteResult(
        simContactsToDeleteResultMap.redactedKeys(), true
    )

    override val isSuccessful: Boolean by unsafeLazy {
        // By default, all returns true when the collection is empty. So, we override that.
        simContactsToDeleteResultMap.run { isNotEmpty() && all { it.value } }
    }

    override fun isSuccessful(simContact: ExistingSimContactEntity): Boolean =
        simContactsToDeleteResultMap.getOrElse(simContact.toSimContactToDelete()) { false }

    override fun isSuccessful(name: String?, number: String?): Boolean =
        simContactsToDeleteResultMap.getOrElse(
            SimContactToDelete(
                name = name,
                number = number
            )
        ) { false }
}

// Allows users to pass in name and number instead of an entity.
// We could also use a Pair but this is more verbose and less prone to error.
// This is pretty much a NewSimContact except the name is different. Maybe we'll just use
// NewSimContact??? IDK. We'll stick to this for now!
private data class SimContactToDelete(
    val name: String?,
    val number: String?,
    override val isRedacted: Boolean = false
) : Redactable {

    override fun redactedCopy() = copy(
        isRedacted = true,

        name = name?.redact(),
        number = number?.redact()
    )
}

private fun ExistingSimContactEntity.toSimContactToDelete() = SimContactToDelete(
    name = name,
    number = number,
    isRedacted = isRedacted
)

/**
 * Returns a where clause that uses the [SimContactToDelete.name] (tag) and
 * [SimContactToDelete.number] to select the contact to delete. This is the only form of selection
 * that is supported. Selecting by _id is not supported because they are not constant.
 *
 * The ID is not used here at all.
 */
private val SimContactToDelete.deleteWhere: String
    // We will not construct the where String using our own Where functions to avoid generating
    // parenthesis, which breaks the way this where clause is processed.
    get() = "tag='${name}' AND number='${number}'"