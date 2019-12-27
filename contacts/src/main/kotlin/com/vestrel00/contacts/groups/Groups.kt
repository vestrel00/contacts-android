package com.vestrel00.contacts.groups

import android.content.Context
import com.vestrel00.contacts.ContactsPermissions

interface Groups {

    fun query(context: Context): GroupsQuery

    fun insert(context: Context): GroupsInsert

    fun update(context: Context): GroupsUpdate

    fun delete(context: Context): GroupsDelete

    fun permissions(context: Context): ContactsPermissions
}

@Suppress("FunctionName")
internal fun Groups(): Groups = GroupsImpl()

private class GroupsImpl : Groups {

    override fun query(context: Context) = GroupsQuery(context)

    override fun insert(context: Context) = GroupsInsert(context)

    override fun update(context: Context) = GroupsUpdate(context)

    override fun delete(context: Context): GroupsDelete = GroupsDelete(context)

    override fun permissions(context: Context) = ContactsPermissions(context)
}