package contacts.entities.custom.pokemon

import contacts.core.entities.custom.CustomDataRegistry

/**
 * Provides functions for consumers to register [Pokemon] components to a [CustomDataRegistry]
 * instance, enabling queries, inserts, updates, and deletes of [Pokemon] data.
 */
class PokemonRegistration : CustomDataRegistry.EntryRegistration {

    /**
     * Registers [Pokemon] components to the given [customDataRegistry], enabling queries, inserts,
     * updates, and deletes of [Pokemon] data.
     */
    override fun registerTo(customDataRegistry: CustomDataRegistry) {
        customDataRegistry.register(PokemonEntry())
    }
}