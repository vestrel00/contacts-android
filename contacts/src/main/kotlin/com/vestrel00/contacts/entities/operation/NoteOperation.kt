package com.vestrel00.contacts.entities.operation

import com.vestrel00.contacts.AbstractField
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MutableNote

internal class NoteOperation : AbstractDataOperation<MutableNote>() {

    override val mimeType = MimeType.NOTE

    override fun setData(
        data: MutableNote, setValue: (field: AbstractField, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Note.Note, data.note)
    }
}