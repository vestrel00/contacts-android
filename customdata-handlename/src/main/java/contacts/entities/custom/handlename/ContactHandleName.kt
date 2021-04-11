package contacts.entities.custom.handlename

import contacts.entities.Contact
import contacts.entities.MutableContact
import contacts.entities.custom.CustomDataRegistry
import contacts.entities.custom.GlobalCustomDataRegistry

/**
 * Returns the sequence of [HandleName]s from all [Contact.rawContacts] ordered by the
 * [HandleName.id].
 */
fun Contact.handleNames(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): Sequence<HandleName> = rawContacts
    .asSequence()
    .flatMap { it.handleNames(customDataRegistry) }
    .sortedBy { it.id }

/**
 * Returns the list of [HandleName]s from all [Contact.rawContacts] ordered by the [HandleName.id].
 */
fun Contact.handleNameList(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): List<HandleName> = handleNames(customDataRegistry).toList()

/**
 * Returns the sequence of [MutableHandleName]s from all [Contact.rawContacts] ordered by the
 * [MutableHandleName.id].
 */
fun MutableContact.handleNames(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): Sequence<MutableHandleName> = rawContacts
    .asSequence()
    .flatMap { it.handleNames(customDataRegistry) }
    .sortedBy { it.id }

/**
 * Returns the list of [MutableHandleName]s from all [Contact.rawContacts] ordered by the
 * [MutableHandleName.id].
 */
fun MutableContact.handleNameList(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): List<MutableHandleName> = handleNames(customDataRegistry).toList()

/**
 * Adds the given [handleName] to the first RawContact in [MutableContact.rawContacts] sorted by
 * the RawContact id.
 *
 * This does not perform the actual update to the database. You will need to perform an update
 * operation on this [MutableContact] object.
 */
fun MutableContact.addHandleName(
    handleName: MutableHandleName,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
) {
    rawContacts.firstOrNull()?.addHandleName(handleName, customDataRegistry)
}

/**
 * Adds a handle name s(configured by [configureHandleName]) to the first RawContact in
 * [MutableContact.rawContacts] sorted by the RawContact id.
 *
 * This does not perform the actual update to the database. You will need to perform an update
 * operation on this [MutableContact] object.
 */
fun MutableContact.addHandleName(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    configureHandleName: MutableHandleName.() -> Unit
) {
    addHandleName(MutableHandleName().apply(configureHandleName), customDataRegistry)
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
    handleName: MutableHandleName,
    byReference: Boolean = false,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
) {
    for (rawContact in rawContacts) {
        customDataRegistry.removeCustomDataEntityFrom(rawContact, byReference, handleName)
    }
}

/**
 * Removes all handle names from all [MutableContact.rawContacts].
 *
 * This does not perform the actual delete to the database. You will need to perform a delete
 * operation on this [MutableContact] object.
 */
fun MutableContact.removeAllHandleNames(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
) {
    for (rawContact in rawContacts) {
        customDataRegistry.removeAllCustomDataEntityFrom(rawContact, HandleNameMimeType)
    }
}