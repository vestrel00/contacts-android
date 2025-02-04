package contacts.entities.custom.multiplenotes

import contacts.core.data.DataQuery
import contacts.core.data.DataQueryFactory

/**
 * Queries for [MultipleNotes]s.
 */
fun DataQueryFactory.multipleNotes(): DataQuery<MultipleNotesField, MultipleNotesFields, MultipleNotes> =
    customData(MultipleNotesMimeType)