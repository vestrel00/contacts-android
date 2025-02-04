package contacts.entities.custom.multiplenotes

import contacts.core.entities.custom.CustomDataRegistry.Entry

// Keep this internal. Consumers don't need to see this stuff. Less visibility the better!
internal class MultipleNotesEntry :
    Entry<MultipleNotesField, MultipleNotesDataCursor, MultipleNotesEntity, MultipleNotes> {

    override val mimeType = MultipleNotesMimeType

    override val fieldSet = MultipleNotesFields

    override val fieldMapper = MultipleNotesFieldMapper()

    override val countRestriction = MULTIPLE_NOTES_COUNT_RESTRICTION

    override val mapperFactory = MultipleNotesMapperFactory()

    override val operationFactory = MultipleNotesOperationFactory()
}