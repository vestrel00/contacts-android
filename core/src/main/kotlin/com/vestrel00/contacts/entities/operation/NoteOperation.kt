package com.vestrel00.contacts.entities.operation

import com.vestrel00.contacts.Field
import com.vestrel00.contacts.Fields
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MutableNote

internal object NoteOperation : AbstractDataOperation<MutableNote>() {

    override val mimeType = MimeType.NOTE

    override fun setData(
        data: MutableNote, setValue: (field: Field, dataValue: Any?) -> Unit
    ) {
        setValue(Fields.Note.Note, data.note)
    }
}