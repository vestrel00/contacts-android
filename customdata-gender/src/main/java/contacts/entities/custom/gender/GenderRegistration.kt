package contacts.entities.custom.gender

import contacts.entities.custom.CustomDataRegistry

/**
 * Provides functions for consumers to register [Gender] components to a [CustomDataRegistry]
 * instance, enabling queries, inserts, updates, and deletes of [Gender] data.
 */
// TODO try this out!!!!
class GenderRegistration : CustomDataRegistry.EntryRegistration {

    /**
     * Registers [Gender] components to the given [customDataRegistry], enabling queries, inserts,
     * updates, and deletes of [Gender] data.
     */
    override fun registerTo(customDataRegistry: CustomDataRegistry) {
        customDataRegistry.register(GenderEntry())
    }
}