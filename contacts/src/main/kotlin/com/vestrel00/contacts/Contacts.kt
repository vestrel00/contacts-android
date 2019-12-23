package com.vestrel00.contacts

import android.content.Context

interface Contacts {

    fun query(context: Context): Query

    fun permissions(context: Context): ContactsPermissions
}

/**
 * Creates a new [Contacts] instance.
 */
@Suppress("FunctionName")
fun Contacts(): Contacts = ContactsImpl()

private class ContactsImpl : Contacts {

    override fun query(context: Context) = Query(context)

    override fun permissions(context: Context) = ContactsPermissions(context)
}
