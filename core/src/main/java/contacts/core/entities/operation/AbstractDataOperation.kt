package contacts.core.entities.operation

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import contacts.core.AbstractDataField
import contacts.core.DataField
import contacts.core.Fields
import contacts.core.Include
import contacts.core.Where
import contacts.core.and
import contacts.core.entities.DataEntity
import contacts.core.entities.MimeType
import contacts.core.entities.NewDataEntity
import contacts.core.entities.cursor.CursorHolder
import contacts.core.entities.cursor.dataCursor
import contacts.core.entities.isNotNullOrBlank
import contacts.core.entities.propertiesAreAllNullOrBlank
import contacts.core.entities.table.Table
import contacts.core.equalTo
import contacts.core.util.dataUri
import contacts.core.util.query
import contacts.core.util.toSqlValue

/**
 * Builds [ContentProviderOperation]s for [Table.Data] using the field [F] and entity [E].
 *
 * ## Include fields
 *
 * Insert and update operations will do nothing (no-op) for data whose corresponding field is not
 * specified in [includeFields]. If [includeFields] is...
 *
 * - null, then the included field checks are disabled. This means that any non-blank data will be
 *   processed. This is a more optimal, recommended way of including all fields.
 * - not null but empty, then data will be skipped (no-op).
 */
abstract class AbstractDataOperation<F : DataField, E : DataEntity>(
    callerIsSyncAdapter: Boolean,
    isProfile: Boolean,
    // Null include field set mean include checks are disabled.
    protected val includeFields: Set<F>?
) {

    internal val contentUri: Uri = dataUri(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile
    )

    protected abstract val mimeType: MimeType

    /**
     * Sets the [data] values into the operation via the provided [setValue] function.
     */
    protected abstract fun setValuesFromData(data: E, setValue: (field: F, value: Any?) -> Unit)

    /**
     * There [Where] clause used as the selection for queries and deletes.
     */
    internal fun selectionWithMimeTypeForRawContact(rawContactId: Long): Where<AbstractDataField> =
        (Fields.MimeType equalTo mimeType) and (Fields.RawContact.Id equalTo rawContactId)

    /**
     * Returns a [ContentProviderOperation] for adding the [entity] properties to the insert
     * operation. The [rawContactIdOpIndex] indicates where in the batch of operations the parent
     * RawContact is being inserted.
     *
     * Returns null if [entity] is blank or no values have been set due to not being included.
     */
    internal fun insertForNewRawContact(
        entity: E,
        rawContactIdOpIndex: Int
    ): ContentProviderOperation? {
        if (entity.isBlank || (includeFields != null && includeFields.isEmpty())) {
            // No-op when entity is blank or no fields are included.
            return null
        }

        val operation = ContentProviderOperation.newInsert(contentUri)
            .withDataIsReadOnly(entity)

        var hasValueSet = false

        setValuesFromData(entity) { field, dataValue ->
            if (
                dataValue.isNotNullOrBlank() &&
                (includeFields == null || includeFields.contains(field))
            ) {
                // Only add the operation if the field should be included.
                // No need to insert null values. Empty values are treated the same as null, same as
                // the AOSP Android Contacts app.
                operation.withValue(field, dataValue)
                hasValueSet = true
            }
        }

        if (!hasValueSet) {
            // If there is actually no data set due to not being included, then do not construct an
            // actual operation.
            return null
        }

        return operation
            // Sets the raw contact id column of this Data table row to the result in the batch
            // operation where the new raw contact is inserted.
            // Note that the Contact ID is automatically set by the Contacts provider.
            .withValueBackReference(Fields.RawContact.Id.columnName, rawContactIdOpIndex)
            // Sets the mimetype, which is the type of data (e.g. email) contained in this
            // row's "data1", "data2", ... columns
            .withValue(Fields.MimeType, mimeType.value)
            .build()
    }

    /**
     * Returns [ContentProviderOperation]s for adding entities to the insert operation. The
     * [rawContactIdOpIndex] indicates where in the batch of operations the parent RawContact is
     * being inserted.
     *
     * Blank entities are excluded.
     */
    internal fun insertForNewRawContact(
        entities: List<E>,
        rawContactIdOpIndex: Int
    ): List<ContentProviderOperation> =
        buildList {
            for (entity in entities) {
                insertForNewRawContact(entity, rawContactIdOpIndex)?.let(::add)
            }
        }

    /**
     * Provides the [ContentProviderOperation] for updating, inserting, or deleting the data row(s)
     * (represented by the [entities]) of the raw contact with the given [rawContactId].
     *
     * Use this function for data rows of a contact with [mimeType] that may occur more than once.
     * For example, a contact may have more than 1 data row for address, email, phone, etc.
     */
    internal fun updateInsertOrDeleteDataForRawContact(
        entities: Collection<E>, rawContactId: Long, contentResolver: ContentResolver
    ): List<ContentProviderOperation> = buildList {
        if (includeFields != null && includeFields.isEmpty()) {
            // No-op when no fields are included.
            return@buildList
        }

        if (!propertiesAreAllNullOrBlank(entities)) {
            // Get all entities with a valid Id, which means they are (or have been) in the DB.
            val validEntitiesMap = mutableMapOf<Long, E>().apply {
                for (entity in entities) {
                    val dataRowId = entity.idOrNull
                    if (dataRowId != null) {
                        put(dataRowId, entity)
                    }
                }
            }

            // Query for all rows of the RawContact with this operation's mimetype in the database.
            contentResolver.dataRowIdsWithMimeTypeForRawContact(rawContactId) {
                val dataCursor = it.dataCursor()
                while (it.moveToNext()) {
                    val dataRowId = dataCursor.dataId

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
                    operation?.let(::add)
                }
            }

            // Insert all remaining data rows in the valid entities that is not in the cursor.
            // Valid entities have a valid id, which means they are already in the DB. In this case,
            // The entity may have been deleted or another entity belonging to a different contact
            // is included here. Blank entities are not inserted.
            val nonBlankValidEntities =
                validEntitiesMap.values.asSequence().filter { !it.isBlank }

            for (entity in nonBlankValidEntities) {
                insertDataRowForRawContact(entity, rawContactId)?.let(::add)
            }

            // Insert all invalid entities.
            // Invalid entities have an invalid id, which means they are newly created entities
            // that are not yet in the DB. Blank entities are not inserted.
            val nonBlankInvalidEntities =
                entities.asSequence().filter { it.idOrNull == null && !it.isBlank }

            for (entity in nonBlankInvalidEntities) {
                insertDataRowForRawContact(entity, rawContactId)?.let(::add)
            }
        } else {
            // Entities' empty or contains no data. Delete all data rows of this type.
            add(deleteDataRowsWithMimeTypeOfRawContact(rawContactId))
        }
    }

    /**
     * Provides the [ContentProviderOperation] for updating, inserting, or deleting the data row
     * (represented by the [entity]) of the raw contact with the given [rawContactId].
     *
     * Use this function for data rows of a contact with [mimeType] that may only occur once.
     * For example, a contact may only have 1 data row for company, name, note, etc.
     */
    internal fun updateInsertOrDeleteDataForRawContact(
        entity: E?, rawContactId: Long, contentResolver: ContentResolver
    ): ContentProviderOperation? = if (includeFields != null && includeFields.isEmpty()) {
        // No-op when no fields are included.
        null
    } else if (entity != null && !entity.isBlank) {
        // Entity contains some data. Query for the (first) row.
        val dataRowId: Long? =
            contentResolver.dataRowIdsWithMimeTypeForRawContact(rawContactId) {
                it.getNextOrNull { it.dataCursor().dataId }
            }

        if (dataRowId != null) {
            // Row exists. Update.
            updateDataRow(entity, dataRowId)
        } else {
            // Row does not exist. Insert.
            insertDataRowForRawContact(entity, rawContactId)
        }
    } else {
        // Entity contains no data. Delete.
        deleteDataRowsWithMimeTypeOfRawContact(rawContactId)
    }

    /**
     * Provides the [ContentProviderOperation] for inserting the data row (represented by the
     * [entity]) for the given RawContact with [rawContactId].
     *
     * Returns null if no values have been set due to not being included.
     */
    internal fun insertDataRowForRawContact(
        entity: E,
        rawContactId: Long
    ): ContentProviderOperation? {
        val operation = ContentProviderOperation.newInsert(contentUri)
            .withValue(Fields.RawContact.Id, rawContactId)
            .withValue(Fields.MimeType, mimeType.value)
            .withDataIsReadOnly(entity)

        var hasValueSet = false

        setValuesFromData(entity) { field, dataValue ->
            if (
                dataValue.isNotNullOrBlank() &&
                (includeFields == null || includeFields.contains(field))
            ) {
                // Only add the operation if the field should be included.
                // No need to insert null values. Empty values are treated the same as null, same as
                // the AOSP Android Contacts app.
                operation.withValue(field, dataValue)
                hasValueSet = true
            }
        }

        if (!hasValueSet) {
            // If there is actually no data set due to not being included, then do not construct an
            // actual operation. This is not just an optimization but also to prevent an exception
            // from being thrown by the operation builder;
            // java.lang.IllegalArgumentException: Empty values
            return null
        }

        return operation.build()
    }

    /**
     * Provides the [ContentProviderOperation] for updating the data row (represented by the
     * [entity]). If the [entity] is blank, it will be deleted instead. Returns null if the
     * [entity] has a null ID or there are no included fields.
     *
     * Use this function for updating an existing data row. If the data row no longer exists in the
     * DB, the operation will fail.
     */
    internal fun updateDataRowOrDeleteIfBlank(entity: E): ContentProviderOperation? =
        entity.idOrNull?.let { dataRowId ->
            if (includeFields != null && includeFields.isEmpty()) {
                null
            } else if (entity.isBlank) {
                deleteDataRowWithId(dataRowId)
            } else {
                updateDataRow(entity, dataRowId)
            }
        }

    /**
     * Provides the [ContentProviderOperation] for updating the data row (represented by the
     * [entity]) with the given [dataRowId].
     *
     * Note that this function does not check if the [entity] is blank. Checking for blanks should
     * be done at the call-site.
     *
     * Returns null if no values have been set due to not being included.
     */
    private fun updateDataRow(entity: E, dataRowId: Long): ContentProviderOperation? {
        val operation = ContentProviderOperation.newUpdate(contentUri)
            .withSelection(Fields.DataId equalTo dataRowId)

        var hasValueSet = false

        setValuesFromData(entity) { field, dataValue ->
            if (includeFields == null || includeFields.contains(field)) {
                // Only add the operation if the field should be included.
                // Intentionally allow to update values to null. Checking for blanks should be done at
                // the call-site.
                operation.withValue(field, dataValue)
                hasValueSet = true
            }
        }

        if (!hasValueSet) {
            // If there is actually no data set due to not being included, then do not construct an
            // actual operation. This is not just an optimization but also to prevent an exception
            // from being thrown by the operation builder;
            // java.lang.IllegalArgumentException: Empty values
            return null
        }

        return operation.build()
    }

    /**
     * Provides the [ContentProviderOperation] for deleting the data rows of type [E] of the
     * RawContact with [rawContactId].
     */
    private fun deleteDataRowsWithMimeTypeOfRawContact(rawContactId: Long): ContentProviderOperation =
        ContentProviderOperation.newDelete(contentUri)
            .withSelection(selectionWithMimeTypeForRawContact(rawContactId))
            .build()

    /**
     * Provides the [ContentProviderOperation] for deleting the data row with the given [dataRowId].
     */
    internal fun deleteDataRowWithId(dataRowId: Long): ContentProviderOperation =
        ContentProviderOperation.newDelete(contentUri)
            .withSelection(Fields.DataId equalTo dataRowId)
            .build()

    /**
     * Provides the [Cursor] to the data rows of type [T] of the RawContact with [rawContactId].
     */
    private fun <T> ContentResolver.dataRowIdsWithMimeTypeForRawContact(
        rawContactId: Long, processCursor: (CursorHolder<AbstractDataField>) -> T
    ) = query(
        contentUri,
        Include(Fields.DataId),
        selectionWithMimeTypeForRawContact(rawContactId),
        processCursor = processCursor
    )

    private fun ContentProviderOperation.Builder.withDataIsReadOnly(entity: E) = apply {
        if (entity is NewDataEntity) {
            // Yes, we are not checking if Fields.DataIsReadyOnly is in the includeFields. We could
            // change includeFields: Set<F> to be includeFields: Set<AbstractDataField> so that
            // it can be included BUT requires repetitive code changes in a bunch of files and
            // additional memory/CPU overhead and may mess up the way includeFields.isEmpty() is
            // used in this file if not done correctly.
            withValue(Fields.DataIsReadOnly, entity.isReadOnly.toSqlValue())
        }
    }
}