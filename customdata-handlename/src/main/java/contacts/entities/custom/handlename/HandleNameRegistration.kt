package contacts.entities.custom.handlename

import contacts.entities.custom.CustomDataRegistry

/**
 * Provides functions for consumers to register [HandleName] components to a [CustomDataRegistry]
 * instance, enabling queries, inserts, updates, and deletes of [HandleName] data.
 */
class HandleNameRegistration : CustomDataRegistry.EntryRegistration {

    /**
     * Registers [HandleName] components to the given [customDataRegistry], enabling queries, inserts,
     * updates, and deletes of [HandleName] data.
     */
    override fun registerTo(customDataRegistry: CustomDataRegistry) {
        customDataRegistry.register(HandleNameEntry())
    }
}