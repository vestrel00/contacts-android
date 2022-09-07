package contacts.core.sim

import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.Entity
import contacts.core.entities.ExistingSimContactEntity
import contacts.core.entities.SimContact
import contacts.core.entities.table.Table
import contacts.core.util.unsafeLazy

/**
 * Deletes one or more SIM contacts from the SIM Contacts table.
 *
 * ## SIM Card state
 *
 * The [SimCardInfo.isReady] is assumed to be true in these examples for brevity. If false, the
 * update will do nothing.
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
     *
     * ## Duplicate entries
     *
     * If there are multiple duplicate contacts in the SIM card with the given [name] and [number],
     * this will only delete one of those contacts.
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
     * ## SIM Card state
     *
     * Requires [SimCardInfo.isReady] to be true.
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

    private val simContactsToDelete: MutableSet<ExistingSimContactEntity> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : SimContactsDelete {

    override fun toString(): String =
        """
            SimContactsDelete {
                simContactsToDelete: $simContactsToDelete
                hasPermission: ${permissions.canUpdateDelete()}
                isSimCardReady: ${simCardInfo.isReady}
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
            SimContact(
                id = Entity.INVALID_ID, // The ID is really unused so we don't care about its value.
                name = name,
                number = number,
                isRedacted = false
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
                simContacts.redactedCopiesOrThis(isRedacted)
            )
        }

    override fun commit(): SimContactsDelete.Result {
        onPreExecute()

        return if (
            simContactsToDelete.isEmpty() ||
            !permissions.canUpdateDelete() ||
            !simCardInfo.isReady
        ) {
            SimContactsDeleteResult(emptyMap())
        } else {
            val results = mutableMapOf<ExistingSimContactEntity, Boolean>()
            for (simContactToDelete in simContactsToDelete) {
                results[simContactToDelete] = contentResolver.deleteSimContact(simContactToDelete)
            }
            SimContactsDeleteResult(results)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

internal fun ContentResolver.deleteSimContact(simContact: ExistingSimContactEntity): Boolean =
    delete(Table.SimContacts.uri, simContact.deleteWhere, null) > 0

private class SimContactsDeleteResult private constructor(
    private val simContactsToDeleteResultMap: Map<ExistingSimContactEntity, Boolean>,
    override val isRedacted: Boolean
) : SimContactsDelete.Result {

    constructor(simContactIdsResultMap: Map<ExistingSimContactEntity, Boolean>) : this(
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
        simContactsToDeleteResultMap.getOrElse(simContact) { false }

    override fun isSuccessful(name: String?, number: String?): Boolean =
        simContactsToDeleteResultMap.getOrElse(
            SimContact(
                id = Entity.INVALID_ID, // The ID is really unused so we don't care about its value.
                name = name,
                number = number,
                isRedacted = false
            ).redactedCopyOrThis(isRedacted)
        ) { false }
}

/**
 * Returns a where clause that uses the [ExistingSimContactEntity.name] (tag) and
 * [ExistingSimContactEntity.number] to select the contact to delete. This is the only form of
 * selection that is supported. Selecting by _id is not supported because they are not constant.
 *
 * The ID is not used here at all.
 */
private val ExistingSimContactEntity.deleteWhere: String
    // We will not construct the where String using our own Where functions to avoid generating
    // parenthesis, which will not be recognized by the IccProvider. Note that passing in null in
    // the right hand side of the '=' will cause the delete to fail.
    get() = if (name != null && number == null) {
        "tag='${name}'"
    } else if (name == null && number != null) {
        "number='${number}'"
    } else {
        // If both name and number are not null, then we good. If both are null, this will probably
        // fail but we don't allow that scenario anyways so this is fine.
        "tag='${name}' AND number='${number}'"
    }