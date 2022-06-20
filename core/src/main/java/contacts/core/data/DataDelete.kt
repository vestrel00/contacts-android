package contacts.core.data

import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.newDelete
import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.ExistingDataEntity
import contacts.core.entities.operation.withSelection
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import contacts.core.util.*
import contacts.core.util.applyBatch
import contacts.core.util.deleteSuccess
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
 * val result = dataDelete.data(existingDataEntities).commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * DataDelete.Result result = dataDelete.data(existingDataEntities).commit();
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
     * Adds the given [dataIds] to the delete queue, which will be deleted on [commit].
     */
    fun dataWithId(vararg dataIds: Long): DataDelete

    /**
     * See [DataDelete.dataWithId].
     */
    fun dataWithId(dataIds: Collection<Long>): DataDelete

    /**
     * See [DataDelete.dataWithId].
     */
    fun dataWithId(dataIds: Sequence<Long>): DataDelete

    /**
     * Deletes all of the data that match the given [where].
     */
    fun dataWhere(where: Where<AbstractDataField>?): DataDelete

    /**
     * Same as [DataDelete.dataWhere] except you have direct access to all properties of [Fields]
     * in the function parameter. Use this to shorten your code.
     */
    fun dataWhere(where: Fields.() -> Where<AbstractDataField>?): DataDelete

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
         * True if all specified or matching data have successfully been deleted. False if even one
         * delete failed.
         *
         * ## [commit] vs [commitInOneTransaction]
         *
         * If you used several of the following in one call,
         *
         * - [data]
         * - [dataWithId]
         * - [dataWhere]
         *
         * then this value may be false even if the data were actually deleted if you used [commit].
         * Using [commitInOneTransaction] does not have this "issue".
         */
        val isSuccessful: Boolean

        /**
         * True if the [data] has been successfully deleted. False otherwise.
         *
         * This is used in conjunction with [DataDelete.data].
         */
        fun isSuccessful(data: ExistingDataEntity): Boolean

        /**
         * True if the data with the given [dataId] has been successfully deleted. False otherwise.
         *
         * This is used in conjunction with [DataDelete.dataWithId].
         */
        fun isSuccessful(dataId: Long): Boolean

        /**
         * True if the delete operation using the given [where] was successful.
         *
         * This is used in conjunction with [DataDelete.dataWhere].
         */
        fun isSuccessful(where: Where<AbstractDataField>): Boolean

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
    private var dataWhere: Where<AbstractDataField>? = null,

    override val isRedacted: Boolean = false
) : DataDelete {

    private val hasNothingToCommit: Boolean
        get() = dataIds.isEmpty() && dataWhere == null

    override fun toString(): String =
        """
            DataDelete {
                isProfile: $isProfile
                dataIds: $dataIds
                dataWhere: $dataWhere
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): DataDelete = DataDeleteImpl(
        contactsApi,
        isProfile = isProfile,
        dataIds = dataIds,
        dataWhere = dataWhere?.redactedCopy(),
        isRedacted = true
    )

    override fun data(vararg data: ExistingDataEntity) = data(data.asSequence())

    override fun data(data: Collection<ExistingDataEntity>) = data(data.asSequence())

    override fun data(data: Sequence<ExistingDataEntity>) = dataWithId(data.map { it.id })

    override fun dataWithId(vararg dataIds: Long) = dataWithId(dataIds.asSequence())

    override fun dataWithId(dataIds: Collection<Long>) = dataWithId(dataIds.asSequence())

    override fun dataWithId(dataIds: Sequence<Long>): DataDelete = apply {
        this.dataIds.addAll(dataIds)
    }

    override fun dataWhere(where: Where<AbstractDataField>?): DataDelete = apply {
        dataWhere = where?.redactedCopyOrThis(isRedacted)
    }

    override fun dataWhere(where: Fields.() -> Where<AbstractDataField>?) = dataWhere(where(Fields))

    override fun commit(): DataDelete.Result {
        onPreExecute()

        return if (!permissions.canUpdateDelete() || hasNothingToCommit) {
            DataDeleteAllResult(isSuccessful = false)
        } else {
            val dataIdsResultMap = mutableMapOf<Long, Boolean>()
            for (dataId in dataIds) {
                dataIdsResultMap[dataId] =
                    if (dataId.isProfileId != isProfile) {
                        // Intentionally fail the operation to ensure that this is only used for
                        // profile or non-profile deletes. Otherwise, operation can succeed. This
                        // is only done to enforce API design.
                        false
                    } else {
                        contentResolver.deleteDataWhere(Fields.DataId equalTo dataId, isProfile)
                    }
            }

            val whereResultMap = mutableMapOf<String, Boolean>()
            dataWhere?.let {
                whereResultMap[it.toString()] = contentResolver.deleteDataWhere(it, isProfile)
            }

            DataDeleteResult(dataIdsResultMap, whereResultMap)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    override fun commitInOneTransaction(): DataDelete.Result {
        onPreExecute()

        return if (!permissions.canUpdateDelete() || hasNothingToCommit) {
            DataDeleteAllResult(isSuccessful = false)
        } else {
            val validDataIds = dataIds.filter { it.isProfileId == isProfile }

            if (dataIds.size != validDataIds.size) {
                // There are some invalid ids or profile or non-profile data ids, fail without
                // performing operation.
                DataDeleteAllResult(isSuccessful = false)
            } else {
                val operations = arrayListOf<ContentProviderOperation>()

                if (validDataIds.isNotEmpty()) {
                    deleteOperationFor(Fields.DataId `in` validDataIds, isProfile)
                        .let(operations::add)
                }

                dataWhere?.let {
                    deleteOperationFor(it, isProfile).let(operations::add)
                }

                DataDeleteAllResult(isSuccessful = contentResolver.applyBatch(operations).deleteSuccess)
            }
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

private fun ContentResolver.deleteDataWhere(
    where: Where<AbstractDataField>, isProfile: Boolean
): Boolean = applyBatch(deleteOperationFor(where, isProfile)).deleteSuccess

private fun deleteOperationFor(
    where: Where<AbstractDataField>, isProfile: Boolean
): ContentProviderOperation = newDelete(if (isProfile) ProfileUris.DATA.uri else Table.Data.uri)
    .withSelection(where)
    .build()

private class DataDeleteResult private constructor(
    private val dataIdsResultMap: Map<Long, Boolean>,
    private var whereResultMap: Map<String, Boolean>,
    override val isRedacted: Boolean
) : DataDelete.Result {

    constructor(
        dataIdsResultMap: Map<Long, Boolean>,
        whereResultMap: Map<String, Boolean>
    ) : this(dataIdsResultMap, whereResultMap, false)

    override fun toString(): String =
        """
            DataDelete.Result {
                isSuccessful: $isSuccessful
                dataIdsResultMap: $dataIdsResultMap
                whereResultMap: $whereResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): DataDelete.Result = DataDeleteResult(
        dataIdsResultMap = dataIdsResultMap,
        whereResultMap = whereResultMap.redactedStringKeys(),
        isRedacted = true
    )

    override val isSuccessful: Boolean by unsafeLazy {
        if (dataIdsResultMap.isEmpty() && whereResultMap.isEmpty()
        ) {
            // Deleting nothing is NOT successful.
            false
        } else {
            // A set has failure if it is NOT empty and one of its entries is false.
            val hasDataFailure = dataIdsResultMap.any { !it.value }
            val hasWhereFailure = whereResultMap.any { !it.value }
            !hasDataFailure && !hasWhereFailure
        }
    }

    override fun isSuccessful(data: ExistingDataEntity): Boolean = isSuccessful(data.id)

    override fun isSuccessful(dataId: Long): Boolean = dataIdsResultMap.getOrElse(dataId) { false }

    override fun isSuccessful(where: Where<AbstractDataField>): Boolean =
        whereResultMap.getOrElse(where.toString()) { false }
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

    override fun isSuccessful(dataId: Long): Boolean = isSuccessful

    override fun isSuccessful(where: Where<AbstractDataField>): Boolean = isSuccessful
}