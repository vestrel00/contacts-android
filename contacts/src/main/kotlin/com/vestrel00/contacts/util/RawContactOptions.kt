package com.vestrel00.contacts.util

import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Include
import com.vestrel00.contacts.entities.*
import com.vestrel00.contacts.entities.cursor.OptionsCursor
import com.vestrel00.contacts.entities.mapper.OptionsMapper
import com.vestrel00.contacts.entities.operation.OptionsOperation
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.equalTo

fun RawContact.options(context: Context): Options = rawContactOptions(id, context)

fun MutableRawContact.options(context: Context): Options = rawContactOptions(id, context)

private fun rawContactOptions(rawContactId: Long, context: Context): Options {
    if (!ContactsPermissions(context).canQuery() || rawContactId == INVALID_ID) {
        return MutableOptions().toOptions()
    }

    val cursor = context.contentResolver.query(
        Table.RAW_CONTACTS.uri,
        Include(Fields.Options).columnNames,
        "${(Fields.RawContact.Id equalTo rawContactId)}",
        null,
        null
    )

    if (cursor != null && cursor.moveToNext()) {
        val options = OptionsMapper(OptionsCursor(cursor)).options

        cursor.close()

        return options
    }

    return MutableOptions().toOptions()
}

fun RawContact.setOptions(context: Context, options: MutableOptions): Boolean =
    setOptions(id, options, context)

fun RawContact.updateOptions(context: Context, update: MutableOptions.() -> Unit): Boolean =
    updateOptions(id, options(context), update, context)

fun MutableRawContact.setOptions(context: Context, options: MutableOptions): Boolean =
    setOptions(id, options, context)

fun MutableRawContact.updateOptions(
    context: Context, update: MutableOptions.() -> Unit
): Boolean = updateOptions(id, options(context), update, context)

private fun setOptions(rawContactId: Long, options: MutableOptions, context: Context): Boolean {
    if (!ContactsPermissions(context).canInsertUpdateDelete() || rawContactId == INVALID_ID) {
        return false
    }

    val operation = OptionsOperation().updateRawContactOptions(rawContactId, options)

    /*
     * Update the ContactOptionsColumns of the RawContact row matching the RawContacts._ID.
     *
     * Perform this single operation in a batch to be consistent with the other CRUD functions.
     */
    try {
        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
    } catch (exception: Exception) {
        return false
    }

    return true
}

private fun updateOptions(
    rawContactId: Long,
    options: Options,
    update: MutableOptions.() -> Unit,
    context: Context
): Boolean {
    val mutableOptions = options.toMutableOptions()
    mutableOptions.update()
    return setOptions(rawContactId, mutableOptions, context)
}