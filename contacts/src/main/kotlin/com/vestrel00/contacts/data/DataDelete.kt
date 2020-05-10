package com.vestrel00.contacts.data

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.DataEntity
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.equalTo

/**
 * Deletes one or more data the Data table.
 *
 * Note that deleting data will not remove it from existing RawContact instances. The RawContact
 * instances must be refreshed to get the most up-to-date data.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All deletes will do nothing if the permission is not granted.
 *
 * ## Usage
 *
 * To delete a [DataEntity];
 *
 * ```kotlin
 * val result = dataDelete
 *      .data(data)
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * DataDelete.Result result = dataDelete
 *      .data(data)
 *      .commit()
 * ```
 */
interface DataDelete {


    /**
     * Adds the given [data] to the delete queue, which will be deleted on [commit].
     */
    fun data(vararg data: DataEntity): DataDelete

    /**
     * See [DataDelete.data].
     */
    fun data(data: Collection<DataEntity>): DataDelete

    /**
     * See [DataDelete.data].
     */
    fun data(data: Sequence<DataEntity>): DataDelete


    /**
     * Deletes the [DataEntity]s in the queue (added via [data]) and returns the [Result].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    interface Result {

        /**
         * True if all data have successfully been deleted. False if even one delete failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [data] has been successfully deleted. False otherwise.
         */
        fun isSuccessful(data: DataEntity): Boolean
    }
}

@Suppress("FunctionName")
internal fun DataDelete(context: Context): DataDelete = DataDeleteImpl(
    context.contentResolver,
    ContactsPermissions(context)
)

private class DataDeleteImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,
    private val dataIds: MutableSet<Long> = mutableSetOf()
) : DataDelete {

    override fun data(vararg data: DataEntity): DataDelete = data(data.asSequence())

    override fun data(data: Collection<DataEntity>): DataDelete = data(data.asSequence())

    override fun data(data: Sequence<DataEntity>): DataDelete = apply {
        dataIds.addAll(data.map { it.id }.filterNotNull())
    }

    override fun commit(): DataDelete.Result {
        if (dataIds.isEmpty() || !permissions.canInsertUpdateDelete()) {
            return DataDeleteFailed
        }

        val dataIdsResultMap = mutableMapOf<Long, Boolean>()
        for (dataId in dataIds) {
            dataIdsResultMap[dataId] = contentResolver.deleteDataWithId(dataId)
        }

        return DataDeleteResult(dataIdsResultMap)
    }
}

private fun ContentResolver.deleteDataWithId(dataId: Long): Boolean {
    val operation = ContentProviderOperation.newDelete(Table.DATA.uri)
        .withSelection("${Fields.Id equalTo dataId}", null)
        .build()

    // Perform this single operation in a batch to be consistent with the other CRUD functions.
    try {
        applyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
    } catch (exception: Exception) {
        return false
    }

    return true
}

private class DataDeleteResult(
    private val dataIdsResultMap: Map<Long, Boolean>
) : DataDelete.Result {

    override val isSuccessful: Boolean by lazy {
        dataIdsResultMap.all { it.value }
    }

    override fun isSuccessful(data: DataEntity): Boolean =
        data.id?.let { dataId ->
            dataIdsResultMap.getOrElse(dataId) { false }
        } ?: false
}

private object DataDeleteFailed : DataDelete.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(data: DataEntity): Boolean = false
}