package contacts.entities.custom.gender

import contacts.Contacts
import contacts.entities.custom.CustomDataRegistry

/**
 * Provides functions for consumers to register [Gender] components to the [Contacts] or
 * [CustomDataRegistry] instance.
 */
object GenderRegistry {

    /**
     * Registers [Gender] components to the given [contacts]'s [CustomDataRegistry], enabling
     * queries, inserts, updates, and deletes to include [Gender] data.
     */
    @JvmStatic
    fun registerTo(contacts: Contacts) {
        registerTo(contacts.customDataRegistry)
    }

    /**
     * Registers [Gender] components to the given [customDataRegistry], enabling queries, inserts,
     * updates, and deletes to include [Gender] data.
     */
    @JvmStatic
    fun registerTo(customDataRegistry: CustomDataRegistry) {
        customDataRegistry.register(GenderEntry())
    }
}