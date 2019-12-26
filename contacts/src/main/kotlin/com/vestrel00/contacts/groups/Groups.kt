package com.vestrel00.contacts.groups

import android.content.Context
import com.vestrel00.contacts.ContactsPermissions

interface Groups {

    fun query(context: Context): GroupsQuery

    /**
     * Returns a new [GroupsInsert] instance.
     */
    fun insert(context: Context): GroupsInsert

    /**
     * Returns a new [GroupsUpdate] instance.
     */
    fun update(context: Context): GroupsUpdate

    /**
     * Returns a new [ContactsPermissions] instance, which provides functions for checking required
     * permissions.
     */
    fun permissions(context: Context): ContactsPermissions
}

/**
 * Creates a new [Groups] instance.
 */
@Suppress("FunctionName")
internal fun Groups(): Groups = GroupsImpl()

private class GroupsImpl : Groups {

    override fun query(context: Context) = GroupsQuery(context)

    override fun insert(context: Context) = GroupsInsert(context)

    override fun update(context: Context) = GroupsUpdate(context)

    override fun permissions(context: Context) = ContactsPermissions(context)
}