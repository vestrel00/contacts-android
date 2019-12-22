package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.MutableRelation
import com.vestrel00.contacts.entities.cursor.RelationCursor

internal class RelationMapper(private val relationCursor: RelationCursor) {

    val relation: MutableRelation
        get() = MutableRelation(
            id = relationCursor.id,
            rawContactId = relationCursor.rawContactId,
            contactId = relationCursor.contactId,

            type = relationCursor.type,
            label = relationCursor.label,

            name = relationCursor.name
        )
}
