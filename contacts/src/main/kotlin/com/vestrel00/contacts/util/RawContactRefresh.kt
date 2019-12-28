package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Query
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.equalTo

fun RawContact.refresh(context: Context, cancel: () -> Boolean = { false }): RawContact? =
    Query(context)
        .where(Fields.RawContactId equalTo id)
        .findFirst(cancel)
        ?.rawContacts
        ?.firstOrNull()

fun MutableRawContact.refresh(
    context: Context, cancel: () -> Boolean = { false }
): MutableRawContact? {
    if (!hasValidId()) {
        return this
    }

    return Query(context)
        .where(Fields.RawContactId equalTo id)
        .findFirst(cancel)
        ?.rawContacts
        ?.firstOrNull()
        ?.toMutableRawContact()
}