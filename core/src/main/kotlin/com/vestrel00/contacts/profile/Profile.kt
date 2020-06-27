package com.vestrel00.contacts.profile

import android.content.Context
import com.vestrel00.contacts.ContactsPermissions

/**
 * Provides new [ProfileQuery], [ProfileInsert], [ProfileUpdate], and [ProfileDelete] instances.
 *
 * ## Permissions
 *
 * - Add the "android.permission.READ_CONTACTS" to the AndroidManifest in order to [query].
 * - Add the "android.permission.WRITE_CONTACTS" to the AndroidManifest in order to [insert],
 * [update], and [delete].
 *
 * Use [permissions] convenience functions to check for required permissions.
 */
interface Profile {

    /**
     * Returns a new [ProfileQuery] instance.
     */
    fun query(context: Context): ProfileQuery

    /**
     * Returns a new [ContactsPermissions] instance, which provides functions for checking required
     * permissions.
     */
    fun permissions(context: Context): ContactsPermissions
}

@Suppress("FunctionName")
internal fun Profile(): Profile = ProfileImpl()

private class ProfileImpl : Profile {

    override fun query(context: Context) = ProfileQuery(context)

    override fun permissions(context: Context) = ContactsPermissions(context)
}