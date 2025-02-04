package contacts.entities.custom.multiplenotes

import contacts.core.entities.custom.CustomDataRegistry

/**
 * Provides functions for consumers to register [MultipleNotes] components to a [CustomDataRegistry]
 * instance, enabling queries, inserts, updates, and deletes of [MultipleNotes] data.
 */
class MultipleNotesRegistration : CustomDataRegistry.EntryRegistration {

    /**
     * Registers [MultipleNotes] components to the given [customDataRegistry], enabling queries,
     * inserts, updates, and deletes of [MultipleNotes] data.
     */
    override fun registerTo(customDataRegistry: CustomDataRegistry) {
        customDataRegistry.register(MultipleNotesEntry())
    }
}