package contacts.core.entities.operation

import android.content.ContentProviderOperation
import android.content.ContentProviderOperation.Builder
import contacts.core.Field
import contacts.core.Where
import contacts.core.entities.table.Table

internal fun newInsert(table: Table<*>): Builder = ContentProviderOperation.newInsert(table.uri)

internal fun newUpdate(table: Table<*>): Builder = ContentProviderOperation.newUpdate(table.uri)

internal fun newDelete(table: Table<*>): Builder = ContentProviderOperation.newDelete(table.uri)

internal fun Builder.withSelection(where: Where<*>?): Builder =
    withSelection(where?.toString(), null)

internal fun Builder.withValue(field: Field, value: Any?): Builder =
    withValue(field.columnName, value)