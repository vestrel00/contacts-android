package contacts.core.entities.operation

import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.Builder
import contacts.core.Field
import contacts.core.Where
import contacts.core.entities.table.Table

internal fun newInsert(table: Table<*>, callerIsSyncAdapter: Boolean): Builder =
    ContentProviderOperation.newInsert(
        if (table is Table.ContactsContractTable) {
            table.uri(callerIsSyncAdapter)
        } else {
            table.uri()
        }
    )

internal fun newUpdate(table: Table<*>, callerIsSyncAdapter: Boolean): Builder =
    ContentProviderOperation.newUpdate(
        if (table is Table.ContactsContractTable) {
            table.uri(callerIsSyncAdapter)
        } else {
            table.uri()
        }
    )

internal fun newDelete(table: Table<*>, callerIsSyncAdapter: Boolean): Builder =
    ContentProviderOperation.newDelete(
        if (table is Table.ContactsContractTable) {
            table.uri(callerIsSyncAdapter)
        } else {
            table.uri()
        }
    )

internal fun Builder.withSelection(where: Where<*>?): Builder =
    withSelection(where?.toString(), null)

internal fun Builder.withValue(field: Field, value: Any?): Builder =
    withValue(field.columnName, value)

/**
 * Invokes [withValue] with the given [field] and [value] if the [field] is in [includeFields] or
 * if [includeFields] is null.
 */
internal fun <F : Field, V : Any> Builder.withIncludedValue(
    includeFields: Set<F>?,
    field: F,
    value: V?
): Builder = if (includeFields == null || includeFields.contains(field)) {
    withValue(field, value)
} else {
    this
}