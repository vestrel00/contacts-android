package com.vestrel00.contacts.data

import android.content.Context
import com.vestrel00.contacts.ContactsPermissions

/**
 * Provides new [DataQuery], [DataUpdate], and [DataDelete] for Profile OR non-Profile (depending on
 * instance) operations.
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
     * Returns a new [DataQuery] instance for Profile OR non-Profile (depending on instance) data
     * queries.
     */
    fun query(): DataQuery

    /**
     * Returns a new [DataUpdate] instance for Profile OR non-Profile (depending on instance) data
     * updates.
     */
    fun update(): DataUpdate

    /**
     * Returns a new [DataDelete] instance for Profile OR non-Profile (depending on instance) data
     * deletes.
     */
    fun delete(): DataDelete

    /**
     * Returns a [ContactsPermissions] instance, which provides functions for checking required
     * permissions.
     */
    val permissions: ContactsPermissions

    /**
     * Reference to the Application's Context for use in extension functions and external library
     * modules. This is safe to hold on to. Not meant for consumer use.
     */
    val applicationContext: Context
}

@Suppress("FunctionName")
internal fun Data(context: Context, isProfile: Boolean): Data = DataImpl(
    context.applicationContext,
    ContactsPermissions(context.applicationContext),
    isProfile
)

private class DataImpl(
    override val applicationContext: Context,
    override val permissions: ContactsPermissions,
    private val isProfile: Boolean
) : Data {

    override fun query() = DataQuery(applicationContext, isProfile)

    override fun update() = DataUpdate(applicationContext, isProfile)

    override fun delete() = DataDelete(applicationContext, isProfile)
}