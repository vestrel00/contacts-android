package com.vestrel00.contacts.util

import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.entities.*
import com.vestrel00.contacts.entities.operation.OptionsOperation

fun Contact.setOptions(context: Context, options: MutableOptions): Boolean =
    setOptions(id, options, context)

fun Contact.updateOptions(context: Context, update: MutableOptions.() -> Unit): Boolean =
    updateOptions(id, options, update, context)

fun MutableContact.setOptions(context: Context, options: MutableOptions): Boolean =
    setOptions(id, options, context)

fun MutableContact.updateOptions(
    context: Context, update: MutableOptions.() -> Unit
): Boolean = updateOptions(id, options, update, context)

private fun setOptions(contactId: Long, options: MutableOptions, context: Context): Boolean {
    if (!ContactsPermissions(context).canInsertUpdateDelete() || contactId == INVALID_ID) {
        return false
    }

    val operation = OptionsOperation().updateContactOptions(contactId, options)

    /*
     * Update the ContactOptionsColumns of the Contact row matching the Contacts._ID.
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
    contactId: Long,
    optionsOptional: Options?,
    update: MutableOptions.() -> Unit,
    context: Context
): Boolean {
    val options = optionsOptional?.toMutableOptions() ?: MutableOptions()
    options.update()
    return setOptions(contactId, options, context)
}