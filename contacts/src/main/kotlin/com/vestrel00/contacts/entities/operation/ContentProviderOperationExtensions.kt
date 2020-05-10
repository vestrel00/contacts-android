package com.vestrel00.contacts.entities.operation

import android.content.ContentProviderOperation.Builder
import com.vestrel00.contacts.AbstractField

// TODO internal fun ContentProviderOperation.newInsert(table: Table): Builder
// TODO internal fun ContentProviderOperation.newUpdate(table: Table): Builder
// TODO internal fun ContentProviderOperation.newDelete(table: Table): Builder
// TODO internal fun Builder.withSelection(where: Where?): Builder

internal fun Builder.withValue(field: AbstractField, value: Any?): Builder =
    withValue(field.columnName, value)