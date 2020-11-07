package com.vestrel00.contacts.data

import android.content.Context
import com.vestrel00.contacts.ContactsPermissions

/**
 * Provides new [DataQuery], [DataUpdate], and [DataDelete] instances.
 *
 * Note that there is no DataInsert as data is required to be associated with a RawContact.
 * See [com.vestrel00.contacts.Insert]
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
    fun query(): DataQuery

    /**
     * Returns a new [DataUpdate] instance.
     */
    fun update(): DataUpdate

    /**
     * Returns a new [DataDelete] instance.
     */
    fun delete(): DataDelete

    /**
     * Returns a new [ContactsPermissions] instance, which provides functions for checking required
     * permissions.
     */
    fun permissions(): ContactsPermissions

    /**
     * Reference to the Application's Context for use in extension functions and external library
     * modules. This is safe to hold on to. Not meant for consumer use.
     */
    val applicationContext: Context
}

@Suppress("FunctionName")
internal fun Data(context: Context): Data = DataImpl(context.applicationContext)

private class DataImpl(override val applicationContext: Context) : Data {

    override fun query() = DataQuery(applicationContext)

    override fun update() = DataUpdate(applicationContext)

    override fun delete() = DataDelete(applicationContext)

    override fun permissions() = ContactsPermissions(applicationContext)
}