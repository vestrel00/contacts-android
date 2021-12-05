package contacts.core.entities.mapper

import contacts.core.entities.Relation
import contacts.core.entities.cursor.RelationCursor

internal class RelationMapper(private val relationCursor: RelationCursor) :
    DataEntityMapper<Relation> {

    override val value: Relation
        get() = Relation(
            id = relationCursor.dataId,
            rawContactId = relationCursor.rawContactId,
            contactId = relationCursor.contactId,

            isPrimary = relationCursor.isPrimary,
            isSuperPrimary = relationCursor.isSuperPrimary,

            type = relationCursor.type,
            label = relationCursor.label,

            name = relationCursor.name
        )
}
