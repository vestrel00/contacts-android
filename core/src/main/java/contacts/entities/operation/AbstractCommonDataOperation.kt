package contacts.entities.operation

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import contacts.*
import contacts.entities.*
import contacts.entities.cursor.EntityCursor
import contacts.entities.cursor.dataCursor
import contacts.entities.table.ProfileUris
import contacts.entities.table.Table
import contacts.util.query

/**
 * Builds [ContentProviderOperation]s for [Table.Data].
 */
abstract class AbstractCommonDataOperation<T : CommonDataEntity>(isProfile: Boolean) {

    internal val contentUri: Uri = if (isProfile) ProfileUris.DATA.uri else Table.Data.uri

    protected abstract val mimeType: MimeType

    /**
     * Sets the [data] values into the operation via the provided [setValue] function.
     */
    protected abstract fun setData(data: T, setValue: (field: CommonDataField, value: Any?) -> Unit)

    /**
     * There [Where] clause used as the selection for queries, updates, and deletes.
     */
    internal fun selection(rawContactId: Long): Where<AbstractDataField> =
        (Fields.MimeType equalTo mimeType) and (Fields.RawContact.Id equalTo rawContactId)

    /**
     * Returns a [ContentProviderOperation] for adding the [entity] properties to the insert
     * operation.  This assumes that this will be used in a batch of operations where the first
     * operation is the insertion of a new RawContact.
     *
     * Returns null if [entity] is blank.
     */
    internal fun insert(entity: T): ContentProviderOperation? {
        if (entity.isBlank) {
            return null
        }

        val operation = ContentProviderOperation.newInsert(contentUri)

        setData(entity) { field, dataValue ->
            if (dataValue.isNotNullOrBlank()) {
                // No need to insert null values. Empty values are treated the same as null, same as
                // the native Android Contacts app.
                operation.withValue(field, dataValue)
            }
        }

        return operation
            // Sets the raw contact id column of this Data table row to the first result of the
            // batch operation, which is assumed to be a new raw contact.
            // Note that the Contact ID is automatically set by the Contacts provider.
            .withValueBackReference(Fields.RawContact.Id.columnName, 0)
            // Sets the mimetype, which is the type of data (e.g. email) contained in this
            // row's "data1", "data2", ... columns
            .withValue(Fields.MimeType, mimeType.value)
            .build()
    }

    /**
     * Returns [ContentProviderOperation]s for adding entities to the insert operation. This assumes
     * that this will be used in a batch of operations where the first operation is the insertion of
     * a new RawContact.
     *
     * Blank entities are excluded.
     */
    internal fun insert(entities: List<T>): List<ContentProviderOperation> =
        mutableListOf<ContentProviderOperation>().apply {
            for (entity in entities) {
                insert(entity)?.let(::add)
            }
        }

    /**
     * Provides the [ContentProviderOperation] for updating, inserting, or deleting the data row(s)
     * (represented by the [entities]) of the raw contact with the given [rawContactId].
     *
     * Use this function for data rows of a contact with [mimeType] that may occur more than once.
     * For example, a contact may have more than 1 data row for address, email, phone, etc.
     */
    internal fun updateInsertOrDelete(
        entities: Collection<T>, rawContactId: Long, contentResolver: ContentResolver
    ): List<ContentProviderOperation> = mutableListOf<ContentProviderOperation>().apply {
        if (!entitiesAreAllBlank(entities)) {
            // Get all entities with a valid Id, which means they are (or have been) in the DB.
            val validEntitiesMap = mutableMapOf<Long, T>().apply {
                for (entity in entities) {
                    val dataRowId = entity.id
                    if (dataRowId != null) {
                        put(dataRowId, entity)
                    }
                }
            }

            // Query for all rows in the database.
            contentResolver.dataRowIdsFor(rawContactId) {
                val dataCursor = it.dataCursor()
                while (it.moveToNext()) {
                    // There should never be a null Data row Id unless there is a programming error
                    // such as not including the id column in the query. We should technically
                    // force unwrap here. However, the Android ecosystem is huge and I wouldn't
                    // rule out null ids. #NO-TRUST.
                    val dataRowId = dataCursor.dataId ?: continue

                    val entity = validEntitiesMap.remove(dataRowId)
                    val operation = if (entity != null && !entity.isBlank) {
                        // If dataRowId is in entities, update if not blank.
                        updateDataRow(entity, dataRowId)
                    } else {
                        // If dataRowId is not in entities or it is but the entity is blank, delete.
                        // Note that we can gather all data rows to be deleted and delete them using
                        // a single operation but code will get messier.
                        deleteDataRowWithId(dataRowId)
                    }
                    add(operation)
                }
            }

            // Insert all remaining data rows in the valid entities that is not in the cursor.
            // Valid entities have a valid id, which means they are already in the DB. In this case,
            // The entity may have been deleted or another entity belonging to a different contact
            // is included here. Blank entities are not inserted.
            val nonBlankValidEntities =
                validEntitiesMap.values.asSequence().filter { !it.isBlank }

            for (entity in nonBlankValidEntities) {
                add(insertDataRow(entity, rawContactId))
            }

            // Insert all invalid entities.
            // Invalid entities have an invalid id, which means they are newly created entities
            // that are not yet in the DB. Blank entities are not inserted.
            val nonBlankInvalidEntities =
                entities.asSequence().filter { it.id == null && !it.isBlank }

            for (entity in nonBlankInvalidEntities) {
                add(insertDataRow(entity, rawContactId))
            }
        } else {
            // Entities' empty or contains no data. Delete all data rows of this type.
            add(deleteDataRows(rawContactId))
        }
    }

    /**
     * Provides the [ContentProviderOperation] for updating, inserting, or deleting the data row
     * (represented by the [entity]) of the raw contact with the given [rawContactId].
     *
     * Use this function for data rows of a contact with [mimeType] that may only occur once.
     * For example, a contact may only have 1 data row for company, name, note, etc.
     */
    internal fun updateInsertOrDelete(
        entity: T?, rawContactId: Long, contentResolver: ContentResolver
    ): ContentProviderOperation =
        if (entity != null && !entity.isBlank) {
            // Entity contains some data. Query for the (first) row.
            val dataRowId: Long? = contentResolver.dataRowIdsFor(rawContactId) {
                it.getNextOrNull { it.dataCursor().dataId }
            }

            if (dataRowId != null) {
                // Row exists. Update.
                updateDataRow(entity, dataRowId)
            } else {
                // Row does not exist. Insert.
                insertDataRow(entity, rawContactId)
            }
        } else {
            // Entity contains no data. Delete.
            deleteDataRows(rawContactId)
        }

    /**
     * Provides the [ContentProviderOperation] for inserting the data row (represented by the
     * [entity]) for the given [RawContact] with [rawContactId].
     */
    internal fun insertDataRow(entity: T, rawContactId: Long): ContentProviderOperation {
        val operation = ContentProviderOperation.newInsert(contentUri)
            .withValue(Fields.RawContact.Id, rawContactId)
            .withValue(Fields.MimeType, mimeType.value)

        setData(entity) { field, dataValue ->
            if (dataValue.isNotNullOrBlank()) {
                // No need to insert null values. Empty values are treated the same as null, same as
                // the native Android Contacts app.
                operation.withValue(field, dataValue)
            }
        }

        return operation.build()
    }

    /**
     * Provides the [ContentProviderOperation] for updating the data row (represented by the
     * [entity]). If the [entity] is blank, it will be deleted instead.  Returns null if the
     * [entity] has a null ID.
     *
     * Use this function for updating an existing data row. If the data row no longer exists in the
     * DB, the operation will fail.
     */
    internal fun updateDataRowOrDeleteIfBlank(entity: T): ContentProviderOperation? =
        entity.id?.let { dataRowId ->
            if (entity.isBlank) {
                deleteDataRowWithId(dataRowId)
            } else {
                updateDataRow(entity, dataRowId)
            }
        }

    /**
     * Provides the [ContentProviderOperation] for updating the data row (represented by the
     * [entity]) with the given [dataRowId].
     *
     * Note that this function does not check if the [entity] is blank.
     */
    private fun updateDataRow(entity: T, dataRowId: Long): ContentProviderOperation {
        val operation = ContentProviderOperation.newUpdate(contentUri)
            .withSelection(Fields.DataId equalTo dataRowId)

        setData(entity) { field, dataValue ->
            // Intentionally allow to update values to null.
            operation.withValue(field, dataValue)
        }

        return operation.build()
    }

    /**
     * Provides the [ContentProviderOperation] for deleting the data rows of type [T] of the
     * [RawContact] with [rawContactId].
     */
    private fun deleteDataRows(rawContactId: Long): ContentProviderOperation =
        ContentProviderOperation.newDelete(contentUri)
            .withSelection(selection(rawContactId))
            .build()

    /**
     * Provides the [ContentProviderOperation] for deleting the data row with the given [dataRowId].
     */
    internal fun deleteDataRowWithId(dataRowId: Long): ContentProviderOperation =
        ContentProviderOperation.newDelete(contentUri)
            .withSelection(Fields.DataId equalTo dataRowId)
            .build()

    /**
     * Provides the [Cursor] to the data rows of type [T] of the [RawContact] with [rawContactId].
     */
    private fun <T> ContentResolver.dataRowIdsFor(
        rawContactId: Long, processCursor: (EntityCursor<AbstractDataField>) -> T
    ) = query(
        contentUri,
        Include(Fields.DataId),
        selection(rawContactId),
        processCursor = processCursor
    )
}