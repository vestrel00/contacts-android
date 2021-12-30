package contacts.core.data

import android.content.ContentProviderOperation.newDelete
import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.ExistingDataEntity
import contacts.core.entities.operation.withSelection
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import contacts.core.util.applyBatch
import contacts.core.util.isProfileId
import contacts.core.util.unsafeLazy

/**
 * Deletes one or more Profile OR non-Profile (depending on instance) data rows the data table.
 *
 * Note that deleting data will not remove it from existing RawContact instances. The RawContact
 * instances must be refreshed to get the most up-to-date data.
 *
 * Deleting data that has already been deleted may return a successful result. However, no delete
 * actually occurred in the Content Provider Data table because the data row no longer existed.
 * This also applies to data that has not yet been inserted.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All deletes will do nothing if the permission is not granted.
 *
 * ## Usage
 *
 * To delete a set of [ExistingDataEntity];
 *
 * ```kotlin
 * val result = dataDelete
 *      .data(existingDataEntities)
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * DataDelete.Result result = dataDelete
 *      .data(existingDataEntities)
 *      .commit()
 * ```
 */
interface DataDelete : CrudApi {

    /**
     * Adds the given [data] to the delete queue, which will be deleted on [commit].
     */
    fun data(vararg data: ExistingDataEntity): DataDelete

    /**
     * See [DataDelete.data].
     */
    fun data(data: Collection<ExistingDataEntity>): DataDelete

    /**
     * See [DataDelete.data].
     */
    fun data(data: Sequence<ExistingDataEntity>): DataDelete

    /**
     * Deletes the [ExistingDataEntity]s in the queue (added via [data]) and returns the [Result].
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
     * Deletes the [ExistingDataEntity]s in the queue (added via [data]) in one transaction. Either ALL
     * deletes succeed or ALL fail.
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
    override fun redactedCopy(): DataDelete

    interface Result : CrudApi.Result {

        /**
         * True if all data have successfully been deleted. False if even one delete failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [data] has been successfully deleted. False otherwise.
         */
        fun isSuccessful(data: ExistingDataEntity): Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun DataDelete(contacts: Contacts, isProfile: Boolean): DataDelete = DataDeleteImpl(
    contacts, isProfile
)

private class DataDeleteImpl(
    override val contactsApi: Contacts,
    private val isProfile: Boolean,

    private val dataIds: MutableSet<Long> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : DataDelete {

    override fun toString(): String =
        """
            DataDelete {
                isProfile: $isProfile
                dataIds: $dataIds
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): DataDelete = DataDeleteImpl(
        contactsApi, isProfile,
        dataIds,
        isRedacted = true
    )

    override fun data(vararg data: ExistingDataEntity) = data(data.asSequence())

    override fun data(data: Collection<ExistingDataEntity>) = data(data.asSequence())

    override fun data(data: Sequence<ExistingDataEntity>): DataDelete = apply {
        dataIds.addAll(data.map { it.id })
    }

    override fun commit(): DataDelete.Result {
        onPreExecute()

        return if (dataIds.isEmpty() || !permissions.canUpdateDelete()) {
            DataDeleteResult(emptyMap())
        } else {
            val dataIdsResultMap = mutableMapOf<Long, Boolean>()
            for (dataId in dataIds) {
                dataIdsResultMap[dataId] =
                    if (dataId.isProfileId != isProfile) {
                        // Intentionally fail the operation to ensure that this is only used for profile
                        // or non-profile deletes. Otherwise, operation can succeed. This is only done
                        // to enforce API design.
                        false
                    } else {
                        contentResolver.deleteDataWithId(dataId)
                    }
            }

            DataDeleteResult(dataIdsResultMap)
        }
            .redactedCopyOrThis(isRedacted)
            .apply { onPostExecute(contactsApi) }
    }

    override fun commitInOneTransaction(): DataDelete.Result {
        onPreExecute()

        // I know this if-else can be folded. But this is way more readable IMO =)
        val isSuccessful = if (dataIds.isEmpty() || !permissions.canUpdateDelete()) {
            false
        } else {
            val validDataIds = dataIds.filter { it.isProfileId == isProfile }

            if (dataIds.size != validDataIds.size) {
                // There are some invalid ids or profile or non-profile data ids, fail without
                // performing operation.
                false
            } else {
                contentResolver.deleteDataRowsWithIds(dataIds, isProfile)
            }
        }

        return DataDeleteAllResult(isSuccessful)
            .redactedCopyOrThis(isRedacted)
            .apply { onPostExecute(contactsApi) }
    }
}

private fun ContentResolver.deleteDataWithId(dataId: Long): Boolean = applyBatch(
    newDelete(if (dataId.isProfileId) ProfileUris.DATA.uri else Table.Data.uri)
        .withSelection(Fields.DataId equalTo dataId)
        .build()
) != null

private fun ContentResolver.deleteDataRowsWithIds(
    dataIds: Collection<Long>, isProfile: Boolean
): Boolean = applyBatch(
    newDelete(if (isProfile) ProfileUris.DATA.uri else Table.Data.uri)
        .withSelection(Fields.DataId `in` dataIds)
        .build()
) != null

private class DataDeleteResult private constructor(
    private val dataIdsResultMap: Map<Long, Boolean>,
    override val isRedacted: Boolean
) : DataDelete.Result {

    constructor(dataIdsResultMap: Map<Long, Boolean>) : this(dataIdsResultMap, false)

    override fun toString(): String =
        """
            DataDelete.Result {
                isSuccessful: $isSuccessful
                dataIdsResultMap: $dataIdsResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): DataDelete.Result = DataDeleteResult(
        dataIdsResultMap,
        isRedacted = true
    )

    override val isSuccessful: Boolean by unsafeLazy {
        // By default, all returns true when the collection is empty. So, we override that.
        dataIdsResultMap.run { isNotEmpty() && all { it.value } }
    }

    override fun isSuccessful(data: ExistingDataEntity): Boolean =
        dataIdsResultMap.getOrElse(data.id) { false }
}

private class DataDeleteAllResult private constructor(
    override val isSuccessful: Boolean,
    override val isRedacted: Boolean
) : DataDelete.Result {

    constructor(isSuccessful: Boolean) : this(
        isSuccessful = isSuccessful,
        isRedacted = false
    )

    override fun toString(): String =
        """
            DataDelete.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): DataDelete.Result = DataDeleteAllResult(
        isSuccessful = isSuccessful,
        isRedacted = true
    )

    override fun isSuccessful(data: ExistingDataEntity): Boolean = isSuccessful
}