package com.vestrel00.contacts.entities.cursor

import android.database.Cursor
import com.vestrel00.contacts.AbstractField

internal fun Cursor.getString(field: AbstractField): String? {
    val index = getColumnIndex(field.columnName)
    return if (index == -1) null else getString(index)
}

internal fun Cursor.getInt(field: AbstractField): Int? {
    val index = getColumnIndex(field.columnName)
    return if (index == -1) null else getInt(index)
}

internal fun Cursor.getLong(field: AbstractField): Long? {
    val index = getColumnIndex(field.columnName)
    return if (index == -1) null else getLong(index)
}