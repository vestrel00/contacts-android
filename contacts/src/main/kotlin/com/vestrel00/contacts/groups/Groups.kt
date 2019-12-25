package com.vestrel00.contacts.groups

import android.content.Context
import com.vestrel00.contacts.ContactsPermissions

interface Groups {

    fun query(context: Context): GroupsQuery

    fun permissions(context: Context): ContactsPermissions
}

@Suppress("FunctionName")
internal fun Groups(): Groups = GroupsImpl()

private class GroupsImpl : Groups {

    override fun query(context: Context) = GroupsQuery(context)

    override fun permissions(context: Context) = ContactsPermissions(context)
}