package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Relation
import com.vestrel00.contacts.entities.cursor.RelationCursor

internal class RelationMapper(private val relationCursor: RelationCursor) : EntityMapper<Relation> {

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
