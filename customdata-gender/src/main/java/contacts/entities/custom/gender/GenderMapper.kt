package contacts.entities.custom.gender

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataEntityMapper

internal class GenderMapperFactory :
    AbstractCustomDataEntityMapper.Factory<GenderField, GenderDataCursor, GenderEntity> {

    override fun create(
        cursor: Cursor, includeFields: Set<GenderField>
    ): AbstractCustomDataEntityMapper<GenderField, GenderDataCursor, GenderEntity> =
        GenderMapper(GenderDataCursor(cursor, includeFields))
}

private class GenderMapper(cursor: GenderDataCursor) :
    AbstractCustomDataEntityMapper<GenderField, GenderDataCursor, Gender>(cursor) {

    override fun value(cursor: GenderDataCursor) = Gender(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        type = cursor.type,
        label = cursor.label
    )
}