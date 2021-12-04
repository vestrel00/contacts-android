package contacts.entities.custom.handlename

import contacts.core.Contacts
import contacts.core.entities.MutableRawContact
import contacts.core.entities.RawContact

// Dev note: The functions that return a List instead of a Sequence are useful for Java consumers
// as they will not have to convert Sequences to List. Also, all are functions instead of properties
// with getters because there are some setters that have to be functions. So all are functions
// to keep uniformity for OCD purposes.

// Another dev note: Receiver signatures are the concrete types instead of the interface type.
// This is done so that consumers gets references to actual concrete types, which may implement
// other interfaces required by APIs in this library.

/**
 * Returns the sequence of [HandleName]s of this RawContact. Empty if none available (e.g. does not
 * exist in the database or was not an included field in the query).
 */
fun RawContact.handleNames(contacts: Contacts): Sequence<HandleName> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<HandleName>(this, HandleNameMimeType)

    return customDataEntities.asSequence()
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
