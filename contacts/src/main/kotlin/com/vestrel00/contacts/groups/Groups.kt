package com.vestrel00.contacts.groups

import android.content.Context
import com.vestrel00.contacts.ContactsPermissions

interface Groups {

    fun permissions(context: Context): ContactsPermissions
}

/**
 * Creates a new [Groups] instance.
 */
@Suppress("FunctionName")
internal fun Groups(): Groups = GroupsImpl()

private class GroupsImpl : Groups {
    
    override fun permissions(context: Context) = ContactsPermissions(context)
}