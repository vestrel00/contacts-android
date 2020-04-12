package com.vestrel00.contacts.groups

import android.content.Context
import com.vestrel00.contacts.ContactsPermissions

/**
 * Creates a new [GroupsQuery], [GroupsInsert], and [GroupsUpdate] instances.
 *
 * ## Permissions
 *
 * - Add the "android.permission.READ_CONTACTS" to the AndroidManifest in order to [query].
 * - Add the "android.permission.WRITE_CONTACTS" to the AndroidManifest in order to [insert] and
 * [update].
 *
 * Use [permissions] convenience functions to check for required permissions.
 *
 * ## Deleting Groups
 *
 * Deleting groups, even user-created groups, is currently not supported due to some limitations
 * imposed by the Contacts Provider. For more details, see the DEV_NOTES "Groups; Deletion" section.
 */
interface Groups {

    /**
     * Returns a new [GroupsQuery] instance.
     */
    fun query(context: Context): GroupsQuery

    /**
     * Returns a new [GroupsInsert] instance.
     */
    fun insert(context: Context): GroupsInsert

    /**
     * Returns a new [GroupsUpdate] instance.
     */
    fun update(context: Context): GroupsUpdate

    /*
    /**
     * Returns a new [GroupsDelete] instance.
     */
    fun delete(context: Context): GroupsDelete
     */

    /**
     * Returns a new [ContactsPermissions] instance, which provides functions for checking required
     * permissions.
     */
    fun permissions(context: Context): ContactsPermissions
}

@Suppress("FunctionName")
internal fun Groups(): Groups = GroupsImpl()

private class GroupsImpl : Groups {

    override fun query(context: Context) = GroupsQuery(context)

    override fun insert(context: Context) = GroupsInsert(context)

    override fun update(context: Context) = GroupsUpdate(context)

    // override fun delete(context: Context): GroupsDelete = GroupsDelete(context)

    override fun permissions(context: Context) = ContactsPermissions(context)
}