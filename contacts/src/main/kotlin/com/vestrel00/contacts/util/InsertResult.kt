package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.*
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact

fun Insert.Result.rawContact(
    context: Context, rawContact: MutableRawContact, cancel: () -> Boolean = { false }
): RawContact? {

    val rawContactId = rawContactId(rawContact) ?: return null

    return Query(context).where(Fields.RawContactId equalTo rawContactId).find(cancel)
        .asSequence()
        .flatMap { it.rawContacts.asSequence() }
        .find { it.id == rawContactId }
}

fun Insert.Result.rawContacts(
    context: Context, cancel: () -> Boolean = { false }
): List<RawContact> = Query(context).where(Fields.RawContactId `in` rawContactIds).find(cancel)
    .asSequence()
    .flatMap { it.rawContacts.asSequence() }
    .filter { rawContactIds.contains(it.id) }
    .toList()

fun Insert.Result.contact(
    context: Context, rawContact: MutableRawContact, cancel: () -> Boolean = { false }
): Contact? {

    val rawContactId = rawContactId(rawContact) ?: return null

    return Query(context).where(Fields.RawContactId equalTo rawContactId).find(cancel)
        .find { contact ->
            contact.rawContacts.find { rawContact ->
                rawContact.id == rawContactId
            } != null
        }
}

fun Insert.Result.contacts(context: Context, cancel: () -> Boolean = { false }): List<Contact> =
    Query(context).where(Fields.RawContactId `in` rawContactIds).find(cancel)
        .filter { contact ->
            contact.rawContacts.find { rawContact ->
                rawContactIds.contains(rawContact.id)
            } != null
        }