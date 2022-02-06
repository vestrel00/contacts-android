package contacts.entities.custom.googlecontacts

import contacts.core.entities.custom.CustomDataRegistry
import contacts.entities.custom.googlecontacts.fileas.FileAsEntry
import contacts.entities.custom.googlecontacts.userdefined.UserDefinedEntry

/**
 * Provides functions for consumers to register
 * [contacts.entities.custom.googlecontacts.fileas.FileAs] and
 * [contacts.entities.custom.googlecontacts.userdefined.UserDefined] components to a
 * [CustomDataRegistry] instance, enabling queries, inserts, updates, and deletes of such data.
 */
class GoogleContactsRegistration : CustomDataRegistry.EntryRegistration {

    /**
     * Registers [contacts.entities.custom.googlecontacts.fileas.FileAs] and
     * [contacts.entities.custom.googlecontacts.userdefined.UserDefined] components to the given
     * [customDataRegistry], enabling queries, inserts, updates, and deletes of such data.
     */
    override fun registerTo(customDataRegistry: CustomDataRegistry) {
        customDataRegistry.register(FileAsEntry(), UserDefinedEntry())
    }
}