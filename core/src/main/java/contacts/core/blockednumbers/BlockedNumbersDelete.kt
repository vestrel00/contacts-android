package contacts.core.blockednumbers

import contacts.core.*
import contacts.core.entities.BlockedNumber
import contacts.core.entities.operation.BlockedNumbersOperation
import contacts.core.util.applyBlockedNumberBatch
import contacts.core.util.unsafeLazy

/**
 * Deletes one or more blocked numbers from the Blocked Numbers table.
 *
 * ## Privileges
 *
 * Requires [BlockedNumbersPrivileges.canReadAndWrite]. All deletes will do nothing if these
 * privileges are not acquired.
 *
 * ## Usage
 *
 * To delete the given blockedNumbers,
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = blockedNumbersDelete.blockedNumbers(blockedNumbers).commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * BlockedNumbersDelete.Result result = blockedNumbersDelete.blockedNumbers(blockedNumbers).commit();
 * ```
 */
interface BlockedNumbersDelete : CrudApi {

    /**
     * Adds the given [blockedNumbers] to the delete queue, which will be deleted on [commit].
     */
    fun blockedNumbers(vararg blockedNumbers: BlockedNumber): BlockedNumbersDelete

    /**
     * See [BlockedNumbersDelete.blockedNumbers].
     */
    fun blockedNumbers(blockedNumbers: Collection<BlockedNumber>): BlockedNumbersDelete

    /**
     * See [BlockedNumbersDelete.blockedNumbers].
     */
    fun blockedNumbers(blockedNumbers: Sequence<BlockedNumber>): BlockedNumbersDelete

    /**
     * Deletes the [BlockedNumber]s in the queue (added via [blockedNumbers]) and returns the
     * [Result].
     *
     * ## Privileges
     *
     * Requires [BlockedNumbersPrivileges.canReadAndWrite].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    /**
     * Deletes the [BlockedNumber]s in the queue (added via [blockedNumbers]) in one transaction.
     * Either ALL deletes succeed or ALL fail.
     *
     * ## Privileges
     *
     * Requires [BlockedNumbersPrivileges.canReadAndWrite].
     *
     * ## Thread Safety

     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commitInOneTransaction(): Result

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
    override fun redactedCopy(): BlockedNumbersDelete

    interface Result : CrudApi.Result {

        /**
         * True if all BlockedNumbers have successfully been deleted. False if even one delete
         * failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [blockedNumber] has been successfully deleted. False otherwise.
         */
        fun isSuccessful(blockedNumber: BlockedNumber): Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun BlockedNumbersDelete(contacts: Contacts): BlockedNumbersDelete =
    BlockedNumbersDeleteImpl(contacts, BlockedNumbersPrivileges(contacts.applicationContext))

private class BlockedNumbersDeleteImpl(
    override val contactsApi: Contacts,
    private val privileges: BlockedNumbersPrivileges,

    private val blockedNumbers: MutableSet<BlockedNumber> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : BlockedNumbersDelete {

    override fun toString(): String =
        """
            BlockedNumbersDelete {
                blockedNumbers: $blockedNumbers
                hasPrivileges: ${privileges.canReadAndWrite()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): BlockedNumbersDelete = BlockedNumbersDeleteImpl(
        contactsApi,
        privileges,

        // Redact blockedNumber data.
        blockedNumbers.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun blockedNumbers(vararg blockedNumbers: BlockedNumber) =
        blockedNumbers(blockedNumbers.asSequence())

    override fun blockedNumbers(blockedNumbers: Collection<BlockedNumber>) =
        blockedNumbers(blockedNumbers.asSequence())

    override fun blockedNumbers(blockedNumbers: Sequence<BlockedNumber>): BlockedNumbersDelete =
        apply {
            this.blockedNumbers.addAll(blockedNumbers.redactedCopiesOrThis(isRedacted))
        }

    override fun commit(): BlockedNumbersDelete.Result {
        onPreExecute()

        return if (blockedNumbers.isEmpty() || !privileges.canReadAndWrite()) {
            BlockedNumbersDeleteResult(emptyMap())
        } else {
            val results = mutableMapOf<Long, Boolean>()
            for (blockedNumber in blockedNumbers) {
                results[blockedNumber.id] = contentResolver.applyBlockedNumberBatch(
                    BlockedNumbersOperation().delete(blockedNumber.id)
                ) != null
            }
            BlockedNumbersDeleteResult(results)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    override fun commitInOneTransaction(): BlockedNumbersDelete.Result {
        onPreExecute()

        val isSuccessful = privileges.canReadAndWrite()
                && blockedNumbers.isNotEmpty()
                && contentResolver.applyBlockedNumberBatch(
            BlockedNumbersOperation().delete(
                blockedNumbers.map { it.id })
        ) != null

        return BlockedNumbersDeleteAllResult(isSuccessful)
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

private class BlockedNumbersDeleteResult private constructor(
    private val blockedNumberIdsResultMap: Map<Long, Boolean>,
    override val isRedacted: Boolean
) : BlockedNumbersDelete.Result {

    constructor(blockedNumberIdsResultMap: Map<Long, Boolean>) : this(
        blockedNumberIdsResultMap,
        false
    )

    override fun toString(): String =
        """
            BlockedNumbersDelete.Result {
                isSuccessful: $isSuccessful
                blockedNumberIdsResultMap: $blockedNumberIdsResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): BlockedNumbersDelete.Result = BlockedNumbersDeleteResult(
        blockedNumberIdsResultMap, true
    )

    override val isSuccessful: Boolean by unsafeLazy {
        // By default, all returns true when the collection is empty. So, we override that.
        blockedNumberIdsResultMap.run { isNotEmpty() && all { it.value } }
    }

    override fun isSuccessful(blockedNumber: BlockedNumber): Boolean {
        return blockedNumberIdsResultMap.getOrElse(blockedNumber.id) { false }
    }
}

private class BlockedNumbersDeleteAllResult private constructor(
    override val isSuccessful: Boolean,
    override val isRedacted: Boolean
) : BlockedNumbersDelete.Result {

    constructor(isSuccessful: Boolean) : this(
        isSuccessful = isSuccessful,
        isRedacted = false
    )

    override fun toString(): String =
        """
            BlockedNumbersDelete.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): BlockedNumbersDelete.Result = BlockedNumbersDeleteAllResult(
        isSuccessful = isSuccessful,
        isRedacted = true
    )

    override fun isSuccessful(blockedNumber: BlockedNumber): Boolean = isSuccessful
}
