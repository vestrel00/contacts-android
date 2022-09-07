package contacts.core.sim

import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.ExistingSimContactEntity
import contacts.core.entities.MutableSimContact
import contacts.core.entities.operation.SimContactsOperation
import contacts.core.entities.table.Table
import contacts.core.sim.SimContactsUpdate.Result.FailureReason
import contacts.core.util.unsafeLazy

/**
 * Updates one or more user SIM contacts in the SIM contacts table.
 *
 * ## Blank contacts are not allowed
 *
 * Blank SimContacts (name AND number are both null or blank) will NOT be updated. The name OR
 * number can be null or blank but not both.
 *
 * ## Character limits
 *
 * The `name` and `number` are subject to the SIM card's maximum character limit, which is typically
 * around 20-30 characters (in modern times). This may vary per SIM card. Inserts or updates will
 * fail if the limit is breached.
 *
 * ## SIM Card state
 *
 * The [SimCardInfo.isReady] is assumed to be true in these examples for brevity. If false, the
 * insert will do nothing.
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
 * In Kotlin,
 *
 * ```kotlin
 * var current: SimContact
 * var modified: MutableSimContact = current.mutableCopy {
 *     // change the name and/or number
 * }
 *
 * val result = update
 *      .simContact(current, modified)
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * SimContact current;
 * MutableSimContact modified = current.mutableCopy();
 * // change the name and/or number
 *
 * SimContactsUpdate.Result result = update
 *      .simContact(current, modified)
 *      .commit();
 * ```
 *
 * **IMPORTANT!** The current entry in the SIM table is not updated based on the ID. Instead, the
 * name AND number are used to lookup the entry to update. Continuing the example above, if you
 * need to make another update, then you must use the modified copy as the current,
 *
 * In Kotlin,
 *
 * ```kotlin
 * current = modified
 * modified = current.newCopy {
 *     // change the name and/or number
 * }
 *
 * val result = update
 *      .simContact(current, modified)
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * current = modified
 * modified = current.newCopy();
 *
 * SimContactsUpdate.Result result = update
 *      .simContact(current, modified)
 *      .commit();
 * ```
 *
 * This limitation comes from Android, not this library.
 */
interface SimContactsUpdate : CrudApi {

    /**
     * Adds an [Entry] in the update queue with the given [current] and [modified] contacts.
     */
    fun simContact(
        current: ExistingSimContactEntity, modified: MutableSimContact
    ): SimContactsUpdate

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
     * ## SIM Card state
     *
     * Requires [SimCardInfo.isReady] to be true.
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
        val current: ExistingSimContactEntity,
        val modified: MutableSimContact,

        override val isRedacted: Boolean
    ) : Redactable {

        constructor(current: ExistingSimContactEntity, modified: MutableSimContact) :
                this(current, modified, false)

        override fun redactedCopy() = copy(
            current = current.redactedCopy(),
            modified = modified.redactedCopy(),

            isRedacted = true
        )
    }

    interface Result : CrudApi.Result {

        /**
         * True if all Entries have successfully been updated. False if even one update failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [simContact] has been successfully updated. False otherwise.
         */
        fun isSuccessful(simContact: ExistingSimContactEntity): Boolean

        /**
         * Returns the reason why the update failed for this [simContact].
         * Null if it did not fail.
         */
        fun failureReason(simContact: ExistingSimContactEntity): FailureReason?

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result

        enum class FailureReason {

            /**
             * The [ExistingSimContactEntity.name] has exceeded the max character limit.
             */
            NAME_EXCEEDED_MAX_CHAR_LIMIT,

            /**
             * The [ExistingSimContactEntity.number] has exceeded the max character limit.
             */
            NUMBER_EXCEEDED_MAX_CHAR_LIMIT,

            /**
             * The [ExistingSimContactEntity.name] and [ExistingSimContactEntity.number] are both blank.
             */
            NAME_AND_NUMBER_ARE_BLANK,

            /**
             * The update failed because of no SIM card in the ready state, no SimContacts
             * specified for insert, number is invalid, etc...
             *
             * ## Dev note
             *
             * We can probably add more reasons instead of just putting all others in the "unknown"
             * bucket. We'll see if consumers need to know about other failure reasons.
             */
            UNKNOWN
        }
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
                isSimCardReady: ${simCardInfo.isReady}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsUpdate = SimContactsUpdateImpl(
        contactsApi,

        // Redact SIM contact data.
        entries.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun simContact(current: ExistingSimContactEntity, modified: MutableSimContact) =
        simContacts(SimContactsUpdate.Entry(current, modified))

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

        return if (
            entries.isEmpty() ||
            !permissions.canUpdateDelete() ||
            !simCardInfo.isReady ||
            cancel()
        ) {
            SimContactsUpdateFailed()
        } else {

            val failureReasons = mutableMapOf<Long, FailureReason?>()

            for (entry in entries) {
                if (cancel()) {
                    break
                }

                val maxCharacterLimits = simCardInfo.maxCharacterLimits()
                failureReasons[entry.current.id] = if (entry.modified.isBlank) {
                    FailureReason.NAME_AND_NUMBER_ARE_BLANK
                } else if (entry.modified.name.length > maxCharacterLimits.nameMaxLength()) {
                    FailureReason.NAME_EXCEEDED_MAX_CHAR_LIMIT
                } else if (entry.modified.number.length > maxCharacterLimits.numberMaxLength()) {
                    FailureReason.NUMBER_EXCEEDED_MAX_CHAR_LIMIT
                } else if (!contentResolver.updateSimContact(
                        entry.current,
                        entry.modified,
                        cancel
                    )
                ) {
                    FailureReason.UNKNOWN
                } else {
                    null
                }
            }

            SimContactsUpdateResult(failureReasons)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

private val String?.length: Int
    get() = this?.let { length } ?: 0

private fun ContentResolver.updateSimContact(
    current: ExistingSimContactEntity,
    modified: MutableSimContact,
    cancel: () -> Boolean
): Boolean {
    val result = SimContactsOperation().update(current, modified)?.let {
        update(Table.SimContacts.uri, it, null, null)
    }

    var updateSuccess = result != null && result > 0

    if (updateSuccess) {
        // If result is not null, query to make sure the row has actually been updated to the
        // modified values. In some devices, the result will be successful but the row has not
        // been modified.
        val updatedSimContact = getSimContacts(cancel).find {
            it.id == modified.id && it.name == modified.name && it.number == modified.number
        }
        if (updatedSimContact == null) {
            updateSuccess = false
        }
    }

    return updateSuccess
}

private class SimContactsUpdateResult private constructor(
    private val failureReasons: Map<Long, FailureReason?>,
    override val isRedacted: Boolean
) : SimContactsUpdate.Result {

    constructor(failureReasons: Map<Long, FailureReason?>) : this(failureReasons, false)

    override fun toString(): String =
        """
            SimContactsUpdate.Result {
                isSuccessful: $isSuccessful
                failureReasons: $failureReasons
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): SimContactsUpdate.Result = SimContactsUpdateResult(
        failureReasons,
        isRedacted = true
    )

    override val isSuccessful: Boolean by unsafeLazy {
        // By default, all returns true when the collection is empty. So, we override that.
        failureReasons.run { isNotEmpty() && all { it.value == null } }
    }

    override fun isSuccessful(simContact: ExistingSimContactEntity): Boolean =
        failureReasons.containsKey(simContact.id) && failureReasons[simContact.id] == null

    override fun failureReason(simContact: ExistingSimContactEntity): FailureReason? =
        failureReasons[simContact.id]
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

    override fun isSuccessful(simContact: ExistingSimContactEntity): Boolean = isSuccessful

    override fun failureReason(simContact: ExistingSimContactEntity) = FailureReason.UNKNOWN
}