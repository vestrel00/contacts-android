package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableRelation
import com.vestrel00.contacts.entities.Relation
import com.vestrel00.contacts.entities.cursor.RelationCursor

internal class RelationMapper(private val relationCursor: RelationCursor) :
    EntityMapper<Relation, MutableRelation> {

    override val toImmutable: Relation
        get() = Relation(
            id = relationCursor.id,
            rawContactId = relationCursor.rawContactId,
            contactId = relationCursor.contactId,

            isPrimary = relationCursor.isPrimary,
            isSuperPrimary = relationCursor.isSuperPrimary,

            type = relationCursor.type,
            label = relationCursor.label,

            name = relationCursor.name
        )

    override val toMutable: MutableRelation
        get() = MutableRelation(
            id = relationCursor.id,
            rawContactId = relationCursor.rawContactId,
            contactId = relationCursor.contactId,

            isPrimary = relationCursor.isPrimary,
            isSuperPrimary = relationCursor.isSuperPrimary,

            type = relationCursor.type,
            label = relationCursor.label,

            name = relationCursor.name
        )
}
