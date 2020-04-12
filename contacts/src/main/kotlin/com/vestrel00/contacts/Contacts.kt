package com.vestrel00.contacts

import android.content.Context
import com.vestrel00.contacts.groups.Groups
import com.vestrel00.contacts.profile.Profile

/**
 * Provides new [Query], [Insert], [Update], [Delete], [Groups], and [Profile] instances.
 *
 * ## Permissions
 *
 * - Add the "android.permission.READ_CONTACTS" to the AndroidManifest in order to [query].
 * - Add the "android.permission.WRITE_CONTACTS" to the AndroidManifest in order to [insert],
 * [update], and [delete].
 *
 * Use [permissions] convenience functions to check for required permissions. The same permissions
 * apply to [Groups].
 *
 * ## Groups
 *
 * For group management, use [groups].
 *
 * ## Profile
 *
 * For user profile management, use [profile].
 */
interface Contacts {

    /**
     * Returns a new [Query] instance.
     */
    fun query(context: Context): Query

    /**
     * Returns a new [QueryData] instance.
     */
    fun queryData(context: Context): QueryData

    /**
     * Returns a new [Insert] instance.
     */
    fun insert(context: Context): Insert

    /**
     * Returns a new [Update] instance.
     */
    fun update(context: Context): Update

    /**
     * Returns a new [Delete] instance.
     */
    fun delete(context: Context): Delete

    /**
     * Returns a new [Groups] instance.
     */
    fun groups(): Groups

    /**
     * Returns a new [Profile] instance.
     */
    fun profile(): Profile

    /**
     * Returns a new [ContactsPermissions] instance, which provides functions for checking required
     * permissions.
     */
    fun permissions(context: Context): ContactsPermissions
}

/**
 * Creates a new [Contacts] instance.
 */
@Suppress("FunctionName")
fun Contacts(): Contacts = ContactsImpl()

private class ContactsImpl : Contacts {

    override fun query(context: Context) = Query(context)

    override fun queryData(context: Context) = QueryData(context)

    override fun insert(context: Context) = Insert(context)

    override fun update(context: Context) = Update(context)

    override fun delete(context: Context) = Delete(context)

    override fun groups() = Groups()

    override fun profile(): Profile = Profile()

    override fun permissions(context: Context) = ContactsPermissions(context)
}
