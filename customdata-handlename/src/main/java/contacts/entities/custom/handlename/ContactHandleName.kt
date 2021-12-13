package contacts.entities.custom.handlename

import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.entities.MutableContact
import contacts.core.util.sortedById

// Dev note: The functions that return a List instead of a Sequence are useful for Java consumers
// as they will not have to convert Sequences to List. Also, all are functions instead of properties
// with getters because there are some setters that have to be functions. So all are functions
// to keep uniformity for OCD purposes.

// region Contact

/**
 * Returns the sequence of [HandleName]s from all [Contact.rawContacts] ordered by id
 */
fun Contact.handleNames(contacts: Contacts): Sequence<HandleName> = rawContacts
    .asSequence()
    .flatMap { it.handleNames(contacts) }
    .sortedBy { it.id }

/**
 * Returns the list of [HandleName]s from all [Contact.rawContacts] ordered by the [HandleName.id].
 */
fun Contact.handleNameList(contacts: Contacts): List<HandleName> = handleNames(contacts).toList()

// endregion

// region MutableContact

/**
 * Returns the sequence of [MutableHandleName]s from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.handleNames(contacts: Contacts): Sequence<MutableHandleNameEntity> = rawContacts
    .asSequence()
    .flatMap { it.handleNames(contacts) }
    .sortedById()

/**
 * Returns the list of [MutableHandleNameEntity]s from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.handleNameList(contacts: Contacts): List<MutableHandleNameEntity> =
    handleNames(contacts).toList()

/**
 * Adds the given [handleName] to the first RawContact in [MutableContact.rawContacts] sorted by
 * the RawContact id.
 */
fun MutableContact.addHandleName(contacts: Contacts, handleName: MutableHandleNameEntity) {
    rawContacts.firstOrNull()?.addHandleName(contacts, handleName)
}

/**
 * Adds a new handle name s(configured by [configureHandleName]) to the first RawContact in
 * [MutableContact.rawContacts] sorted by the RawContact id.
 */
fun MutableContact.addHandleName(
    contacts: Contacts,
    configureHandleName: NewHandleName.() -> Unit
) {
    addHandleName(contacts, NewHandleName().apply(configureHandleName))
}

/**
 * Removes all instances of the given [handleName] from all [MutableContact.rawContacts].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun MutableContact.removeHandleName(
    contacts: Contacts,
    handleName: MutableHandleNameEntity,
    byReference: Boolean = false
) {
    for (rawContact in rawContacts) {
        contacts.customDataRegistry.removeCustomDataEntityFrom(rawContact, byReference, handleName)
    }
}

/**
 * Removes all handle names from all [MutableContact.rawContacts].
 */
fun MutableContact.removeAllHandleNames(contacts: Contacts) {
    for (rawContact in rawContacts) {
        contacts.customDataRegistry.removeAllCustomDataEntityFrom(rawContact, HandleNameMimeType)
    }
}

// endregion