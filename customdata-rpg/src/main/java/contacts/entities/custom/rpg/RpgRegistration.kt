package contacts.entities.custom.rpg

import contacts.core.entities.custom.CustomDataRegistry
import contacts.entities.custom.rpg.profession.RpgProfessionEntry
import contacts.entities.custom.rpg.stats.RpgStatsEntry

/**
 * Provides functions for consumers to register
 * [contacts.entities.custom.rpg.profession.RpgProfession] and
 * [contacts.entities.custom.rpg.stats.RpgStats] components to a
 * [CustomDataRegistry] instance, enabling queries, inserts, updates, and deletes of such data.
 */
class RpgRegistration : CustomDataRegistry.EntryRegistration {

    /**
     * Registers [contacts.entities.custom.rpg.profession.RpgProfession] and
     * [contacts.entities.custom.rpg.stats.RpgStats]  components to the given
     * [customDataRegistry], enabling queries, inserts, updates, and deletes of such data.
     */
    override fun registerTo(customDataRegistry: CustomDataRegistry) {
        customDataRegistry.register(RpgProfessionEntry(), RpgStatsEntry())
    }
}