package contacts.entities.custom.gender

import contacts.entities.custom.AbstractCustomEntityMapper

internal class GenderMapper(cursor: GenderDataCursor) :
    AbstractCustomEntityMapper<GenderField, GenderDataCursor, MutableGender>(cursor) {

    override fun value(cursor: GenderDataCursor) = MutableGender(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        type = cursor.type,
        label = cursor.label
    )
}