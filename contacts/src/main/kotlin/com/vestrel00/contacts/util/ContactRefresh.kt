package com.vestrel00.contacts.util

import android.content.Context
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.Query
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.equalTo

fun Contact.refresh(context: Context, cancel: () -> Boolean = { false }): Contact? = Query(context)
    .where(Fields.Contact.Id equalTo id)
    .findFirst(cancel)

fun MutableContact.refresh(context: Context, cancel: () -> Boolean = { false }): MutableContact? {
    if (!hasValidId()) {
        return this
    }

    return Query(context)
        .where(Fields.Contact.Id equalTo id)
        .findFirst(cancel)
        ?.toMutableContact()
}