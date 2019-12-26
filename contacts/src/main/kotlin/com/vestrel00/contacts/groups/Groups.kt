package com.vestrel00.contacts.groups

import android.content.Context
import com.vestrel00.contacts.ContactsPermissions

interface Groups {

    fun query(context: Context): GroupsQuery

    fun insert(context: Context): GroupsInsert

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

    override fun permissions(context: Context) = ContactsPermissions(context)
}