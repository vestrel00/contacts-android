package contacts.entities.custom.rpg.profession

import android.database.Cursor
import contacts.core.entities.custom.AbstractCustomDataEntityMapper
import contacts.entities.custom.rpg.RpgProfessionField

internal class RpgProfessionMapperFactory :
    AbstractCustomDataEntityMapper.Factory<RpgProfessionField, RpgProfessionDataCursor, RpgProfession> {

    override fun create(
        cursor: Cursor, includeFields: Set<RpgProfessionField>
    ): AbstractCustomDataEntityMapper<RpgProfessionField, RpgProfessionDataCursor, RpgProfession> =
        RpgProfessionMapper(RpgProfessionDataCursor(cursor, includeFields))
}

private class RpgProfessionMapper(cursor: RpgProfessionDataCursor) :
    AbstractCustomDataEntityMapper<RpgProfessionField, RpgProfessionDataCursor, RpgProfession>(
        cursor
    ) {

    override fun value(cursor: RpgProfessionDataCursor) = RpgProfession(
        id = cursor.dataId,
        rawContactId = cursor.rawContactId,
        contactId = cursor.contactId,

        isPrimary = cursor.isPrimary,
        isSuperPrimary = cursor.isSuperPrimary,

        title = cursor.title,

        isRedacted = false
    )
}