package contacts.core.blockednumbers

import android.content.ContentProviderOperation
import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.BlockedNumber
import contacts.core.entities.operation.withSelection
import contacts.core.entities.table.Table
import contacts.core.util.applyBlockedNumberBatch
import contacts.core.util.deleteSuccess
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
     * Adds the given [blockedNumbers] to the delete queue, which will be deleted on [commit] or
     * [commitInOneTransaction].
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
     * Adds the given [blockedNumbersIds] to the delete queue, which will be deleted on [commit]
     * or [commitInOneTransaction].
     */
    fun blockedNumbersWithId(vararg blockedNumbersIds: Long): BlockedNumbersDelete

    /**
     * See [BlockedNumbersDelete.blockedNumbersWithId].
     */
    fun blockedNumbersWithId(blockedNumbersIds: Collection<Long>): BlockedNumbersDelete

    /**
     * See [BlockedNumbersDelete.blockedNumbersWithId].
     */
    fun blockedNumbersWithId(blockedNumbersIds: Sequence<Long>): BlockedNumbersDelete

    /**
     * Deletes all of the blocked numbers that match the given [where].
     */
    fun blockedNumbersWhere(where: Where<BlockedNumbersField>?): BlockedNumbersDelete

    /**
     * Same as [BlockedNumbersDelete.blockedNumbersWhere] except you have direct access to all
     * properties of [BlockedNumbersFields] in the function parameter. Use this to shorten your code.
     */
    fun blockedNumbersWhere(
        where: BlockedNumbersFields.() -> Where<BlockedNumbersField>?
    ): BlockedNumbersDelete

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
         *
         * ## [commit] vs [commitInOneTransaction]
         *
         * If you used several of the following in one call,
         *
         * - [blockedNumbers]
         * - [blockedNumbersWithId]
         * - [blockedNumbersWhere]
         *
         * then this value may be false even if the blocked numbers were actually deleted if you
         * used [commit]. Using [commitInOneTransaction] does not have this "issue".
         */
        val isSuccessful: Boolean

        /**
         * True if the [blockedNumber] has been successfully deleted. False otherwise.
         */
        fun isSuccessful(blockedNumber: BlockedNumber): Boolean

        /**
         * True if the blocked number with the given [blockedNumberId] has been successfully
         * deleted. False otherwise.
         *
         * This is used in conjunction with [BlockedNumbersDelete.blockedNumbersWithId].
         */
        fun isSuccessful(blockedNumberId: Long): Boolean

        /**
         * True if the delete operation using the given [where] was successful.
         *
         * This is used in conjunction with [BlockedNumbersDelete.blockedNumbersWhere].
         */
        fun isSuccessful(where: Where<BlockedNumbersField>): Boolean

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

    private val blockedNumbersIds: MutableSet<Long> = mutableSetOf(),
    private var blockedNumbersWhere: Where<BlockedNumbersField>? = null,

    override val isRedacted: Boolean = false
) : BlockedNumbersDelete {

    private val hasNothingToCommit: Boolean
        get() = blockedNumbersIds.isEmpty() && blockedNumbersWhere == null

    override fun toString(): String =
        """
            BlockedNumbersDelete {
                blockedNumbersIds: $blockedNumbersIds
                blockedNumbersWhere: $blockedNumbersWhere
                hasPrivileges: ${privileges.canReadAndWrite()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): BlockedNumbersDelete = BlockedNumbersDeleteImpl(
        contactsApi,
        privileges,

        blockedNumbersIds,
        blockedNumbersWhere?.redactedCopy(),

        isRedacted = true
    )

    override fun blockedNumbers(vararg blockedNumbers: BlockedNumber) =
        blockedNumbers(blockedNumbers.asSequence())

    override fun blockedNumbers(blockedNumbers: Collection<BlockedNumber>) =
        blockedNumbers(blockedNumbers.asSequence())

    override fun blockedNumbers(blockedNumbers: Sequence<BlockedNumber>) =
        blockedNumbersWithId(blockedNumbers.map { it.id })

    override fun blockedNumbersWithId(vararg blockedNumbersIds: Long) =
        blockedNumbersWithId(blockedNumbersIds.asSequence())

    override fun blockedNumbersWithId(blockedNumbersIds: Collection<Long>) =
        blockedNumbersWithId(blockedNumbersIds.asSequence())

    override fun blockedNumbersWithId(blockedNumbersIds: Sequence<Long>): BlockedNumbersDelete =
        apply {
            this.blockedNumbersIds.addAll(blockedNumbersIds)
        }

    override fun blockedNumbersWhere(where: Where<BlockedNumbersField>?): BlockedNumbersDelete =
        apply {
            blockedNumbersWhere = where?.redactedCopyOrThis(isRedacted)
        }

    override fun blockedNumbersWhere(
        where: BlockedNumbersFields.() -> Where<BlockedNumbersField>?
    ) = blockedNumbersWhere(where(BlockedNumbersFields))

    override fun commit(): BlockedNumbersDelete.Result {
        onPreExecute()

        return if (!privileges.canReadAndWrite() || hasNothingToCommit) {
            BlockedNumbersDeleteAllResult(isSuccessful = false)
        } else {
            val results = mutableMapOf<Long, Boolean>()
            for (blockedNumberId in blockedNumbersIds) {
                results[blockedNumberId] = contentResolver.deleteBlockedNumbersWhere(
                    BlockedNumbersFields.Id equalTo blockedNumberId
                )
            }

            val whereResultMap = mutableMapOf<String, Boolean>()
            blockedNumbersWhere?.let {
                whereResultMap[it.toString()] = contentResolver.deleteBlockedNumbersWhere(it)
            }

            BlockedNumbersDeleteResult(results, whereResultMap)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    override fun commitInOneTransaction(): BlockedNumbersDelete.Result {
        onPreExecute()

        return if (!privileges.canReadAndWrite() || hasNothingToCommit) {
            BlockedNumbersDeleteAllResult(isSuccessful = false)
        } else {
            val operations = arrayListOf<ContentProviderOperation>()

            if (blockedNumbersIds.isNotEmpty()) {
                deleteOperationFor(BlockedNumbersFields.Id `in` blockedNumbersIds)
                    .let(operations::add)
            }

            blockedNumbersWhere?.let {
                deleteOperationFor(it).let(operations::add)
            }

            BlockedNumbersDeleteAllResult(
                isSuccessful = contentResolver.applyBlockedNumberBatch(
                    operations
                ).deleteSuccess
            )
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

private fun ContentResolver.deleteBlockedNumbersWhere(where: Where<BlockedNumbersField>): Boolean =
    applyBlockedNumberBatch(deleteOperationFor(where)).deleteSuccess

private fun deleteOperationFor(where: Where<BlockedNumbersField>): ContentProviderOperation =
    ContentProviderOperation.newDelete(Table.BlockedNumbers.uri)
        .withSelection(where)
        .build()

private class BlockedNumbersDeleteResult private constructor(
    private val blockedNumberIdsResultMap: Map<Long, Boolean>,
    private var whereResultMap: Map<String, Boolean>,
    override val isRedacted: Boolean
) : BlockedNumbersDelete.Result {

    constructor(
        blockedNumberIdsResultMap: Map<Long, Boolean>,
        whereResultMap: Map<String, Boolean>
    ) : this(blockedNumberIdsResultMap, whereResultMap, false)

    override fun toString(): String =
        """
            BlockedNumbersDelete.Result {
                isSuccessful: $isSuccessful
                blockedNumberIdsResultMap: $blockedNumberIdsResultMap,
                whereResultMap: $whereResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): BlockedNumbersDelete.Result = BlockedNumbersDeleteResult(
        blockedNumberIdsResultMap = blockedNumberIdsResultMap,
        whereResultMap = whereResultMap.redactedStringKeys(),
        isRedacted = true
    )

    override val isSuccessful: Boolean by unsafeLazy {
        if (blockedNumberIdsResultMap.isEmpty() && whereResultMap.isEmpty()
        ) {
            // Deleting nothing is NOT successful.
            false
        } else {
            // A set has failure if it is NOT empty and one of its entries is false.
            val hasIdFailure = blockedNumberIdsResultMap.any { !it.value }
            val hasWhereFailure = whereResultMap.any { !it.value }
            !hasIdFailure && !hasWhereFailure
        }
    }

    override fun isSuccessful(blockedNumber: BlockedNumber): Boolean {
        return blockedNumberIdsResultMap.getOrElse(blockedNumber.id) { false }
    }

    override fun isSuccessful(blockedNumberId: Long): Boolean =
        blockedNumberIdsResultMap.getOrElse(blockedNumberId) { false }

    override fun isSuccessful(where: Where<BlockedNumbersField>): Boolean =
        whereResultMap.getOrElse(where.toString()) { false }
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

    override fun isSuccessful(blockedNumberId: Long): Boolean = isSuccessful

    override fun isSuccessful(where: Where<BlockedNumbersField>): Boolean = isSuccessful
}
