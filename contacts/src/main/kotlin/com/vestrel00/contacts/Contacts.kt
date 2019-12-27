package com.vestrel00.contacts

import android.content.Context
import com.vestrel00.contacts.groups.Groups

interface Contacts {

    /**
     * Returns a new [Query] instance.
     */
    fun query(context: Context): Query

    /**
     * Returns a new [Insert] instance.
     */
    fun insert(context: Context): Insert

    /**
     * Returns a new [Delete] instance.
     */
    fun delete(context: Context): Delete

    /**
     * Returns a new [ContactsPermissions] instance, which provides functions for checking required
     * permissions.
     */
    fun permissions(context: Context): ContactsPermissions

    /**
     * Returns a new [Groups] instance.
     */
    fun groups(): Groups
}

/**
 * Creates a new [Contacts] instance.
 */
@Suppress("FunctionName")
fun Contacts(): Contacts = ContactsImpl()

private class ContactsImpl : Contacts {

    override fun query(context: Context) = Query(context)

    override fun insert(context: Context) = Insert(context)

    override fun delete(context: Context) = Delete(context)

    override fun permissions(context: Context) = ContactsPermissions(context)

    override fun groups() = Groups()
}
