package contacts.entities.custom.handlename

import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.entities.MutableContact

/**
 * Returns the sequence of [HandleName]s from all [Contact.rawContacts] ordered by the
 * [HandleName.id].
 */
fun Contact.handleNames(contacts: Contacts): Sequence<HandleName> = rawContacts
    .asSequence()
    .flatMap { it.handleNames(contacts) }
    .sortedBy { it.id }

/**
 * Returns the list of [HandleName]s from all [Contact.rawContacts] ordered by the [HandleName.id].
 */
fun Contact.handleNameList(contacts: Contacts): List<HandleName> = handleNames(contacts).toList()

/**
 * Returns the sequence of [MutableHandleName]s from all [Contact.rawContacts] ordered by the
 * [MutableHandleName.id].
 */
fun MutableContact.handleNames(contacts: Contacts): Sequence<MutableHandleName> = rawContacts
    .asSequence()
    .flatMap { it.handleNames(contacts) }
    .sortedBy { it.id }

/**
 * Returns the list of [MutableHandleName]s from all [Contact.rawContacts] ordered by the
 * [MutableHandleName.id].
 */
fun MutableContact.handleNameList(contacts: Contacts): List<MutableHandleName> =
    handleNames(contacts).toList()

/**
 * Adds the given [handleName] to the first RawContact in [MutableContact.rawContacts] sorted by
 * the RawContact id.
 *
 * This does not perform the actual update to the database. You will need to perform an update
 * operation on this [MutableContact] object.
 */
fun MutableContact.addHandleName(contacts: Contacts, handleName: MutableHandleName) {
    rawContacts.firstOrNull()?.addHandleName(contacts, handleName)
}

/**
 * Adds a handle name s(configured by [configureHandleName]) to the first RawContact in
 * [MutableContact.rawContacts] sorted by the RawContact id.
 *
 * This does not perform the actual update to the database. You will need to perform an update
 * operation on this [MutableContact] object.
 */
fun MutableContact.addHandleName(
    contacts: Contacts,
    configureHandleName: MutableHandleName.() -> Unit
) {
    addHandleName(contacts, MutableHandleName().apply(configureHandleName))
}

/**
 * Removes all instances of the given [handleName] from all [MutableContact.rawContacts].
 *
 * This does not perform the actual delete to the database. You will need to perform a delete
 * operation on this [MutableContact] object.
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun MutableContact.removeHandleName(
    contacts: Contacts,
    handleName: MutableHandleName,
    byReference: Boolean = false
) {
    for (rawContact in rawContacts) {
        contacts.customDataRegistry.removeCustomDataEntityFrom(rawContact, byReference, handleName)
    }
}

/**
 * Removes all handle names from all [MutableContact.rawContacts].
 *
 * This does not perform the actual delete to the database. You will need to perform a delete
 * operation on this [MutableContact] object.
 */
fun MutableContact.removeAllHandleNames(contacts: Contacts) {
    for (rawContact in rawContacts) {
        contacts.customDataRegistry.removeAllCustomDataEntityFrom(rawContact, HandleNameMimeType)
    }
}