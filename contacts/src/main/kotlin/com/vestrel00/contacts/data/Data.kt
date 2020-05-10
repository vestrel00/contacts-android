package com.vestrel00.contacts.data

import android.content.Context
import com.vestrel00.contacts.ContactsPermissions

// TODO Implement DataUpdate
/**
 * Provides new [DataQuery], [DataUpdate], and [DataDelete] instances.
 *
 * ## Permissions
 *
 * - Add the "android.permission.READ_CONTACTS" to the AndroidManifest in order to [query].
 * - Add the "android.permission.WRITE_CONTACTS" to the AndroidManifest in order to [update] and
 * [delete].
 *
 * Use [permissions] convenience functions to check for required permissions.
 */
interface Data {

    /**
     * Returns a new [DataQuery] instance.
     */
    fun query(context: Context): DataQuery

    /**
     * Returns a new [DataDelete] instance.
     */
    fun delete(context: Context): DataDelete

    /**
     * Returns a new [ContactsPermissions] instance, which provides functions for checking required
     * permissions.
     */
    fun permissions(context: Context): ContactsPermissions
}

@Suppress("FunctionName")
internal fun Data(): Data = DataImpl()

private class DataImpl : Data {

    override fun query(context: Context) = DataQuery(context)

    override fun delete(context: Context) = DataDelete(context)

    override fun permissions(context: Context) = ContactsPermissions(context)
}