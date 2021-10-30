package contacts.entities.custom.handlename

import contacts.core.Contacts
import contacts.core.entities.MutableRawContact
import contacts.core.entities.RawContact

/**
 * Returns the sequence of [HandleName]s of this RawContact. Empty if none available (e.g. does not
 * exist in the database or was not an included field in the query).
 */
fun RawContact.handleNames(contacts: Contacts): Sequence<HandleName> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<MutableHandleName>(this, HandleNameMimeType)

    return customDataEntities.asSequence().map { it.toHandleName() }
}

/**
 * Returns the list of [HandleName]s of this RawContact. Empty if none available (e.g. does not
 * exist in the database or was not an included field in the query).
 */
fun RawContact.handleNameList(contacts: Contacts): List<HandleName> = handleNames(contacts).toList()

/**
 * Returns the sequence of [MutableHandleName]s of this RawContact. Empty if none available (e.g. does not
 * exist in the database or was not an included field in the query).
 */
fun MutableRawContact.handleNames(contacts: Contacts): Sequence<MutableHandleName> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<MutableHandleName>(this, HandleNameMimeType)

    return customDataEntities.asSequence()
}

/**
 * Returns the list of [MutableHandleName]s of this RawContact. Empty if none available (e.g. does not
 * exist in the database or was not an included field in the query).
 */
fun MutableRawContact.handleNameList(contacts: Contacts): List<MutableHandleName> =
    handleNames(contacts).toList()

/**
 * Adds the given [handleName] to this RawContact.
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableRawContact] object.
 */
fun MutableRawContact.addHandleName(contacts: Contacts, handleName: MutableHandleName) {
    contacts.customDataRegistry.putCustomDataEntityInto(this, handleName)
}

/**
 * Adds a handle name (configured by [configureHandleName]) to this RawContact.
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableRawContact] object.
 */
fun MutableRawContact.addHandleName(
    contacts: Contacts,
    configureHandleName: MutableHandleName.() -> Unit
) {
    addHandleName(contacts, MutableHandleName().apply(configureHandleName))
}

/**
 * Removes all instances of the given [handleName] from this RawContact.
 *
 * This does not perform the actual delete to the database. You will need to perform a delete
 * operation on this [MutableRawContact] object.
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun MutableRawContact.removeHandleName(
    contacts: Contacts,
    handleName: MutableHandleName,
    byReference: Boolean = false
) {
    contacts.customDataRegistry.removeCustomDataEntityFrom(this, byReference, handleName)
}

/**
 * Removes all handle names from this RawContact.
 *
 * This does not perform the actual delete to the database. You will need to perform a delete
 * operation on this [MutableRawContact] object.
 */
fun MutableRawContact.removeAllHandleNames(contacts: Contacts) {
    contacts.customDataRegistry.removeAllCustomDataEntityFrom(this, HandleNameMimeType)
}
