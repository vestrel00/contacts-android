package contacts.entities.custom.handlename

import contacts.core.Contacts
import contacts.core.entities.MutableRawContact
import contacts.core.entities.NewRawContact
import contacts.core.entities.RawContact

// Dev note: The functions that return a List instead of a Sequence are useful for Java consumers
// as they will not have to convert Sequences to List. Also, all are functions instead of properties
// with getters because there are some setters that have to be functions. So all are functions
// to keep uniformity for OCD purposes.

// region RawContact

/**
 * Returns the sequence of [HandleName]s of this RawContact.
 */
fun RawContact.handleNames(contacts: Contacts): Sequence<HandleName> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<HandleName>(this, HandleNameMimeType)

    return customDataEntities.asSequence()
}

/**
 * Returns the list of [HandleName]s of this RawContact.
 */
fun RawContact.handleNameList(contacts: Contacts): List<HandleName> = handleNames(contacts).toList()

// endregion

// region MutableRawContact

/**
 * Returns the sequence of [MutableHandleNameEntity]s of this RawContact.
 */
fun MutableRawContact.handleNames(contacts: Contacts): Sequence<MutableHandleNameEntity> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<MutableHandleNameEntity>(this, HandleNameMimeType)

    return customDataEntities.asSequence()
}

/**
 * Returns the list of [MutableHandleNameEntity]s of this RawContact.
 */
fun MutableRawContact.handleNameList(contacts: Contacts): List<MutableHandleNameEntity> =
    handleNames(contacts).toList()

/**
 * Adds the given [handleName] to this RawContact.
 */
fun MutableRawContact.addHandleName(contacts: Contacts, handleName: MutableHandleNameEntity) {
    contacts.customDataRegistry.putCustomDataEntityInto(this, handleName)
}

/**
 * Adds a handle name (configured by [configureHandleName]) to this RawContact.
 */
fun MutableRawContact.addHandleName(
    contacts: Contacts,
    configureHandleName: NewHandleName.() -> Unit
) {
    addHandleName(contacts, NewHandleName().apply(configureHandleName))
}

/**
 * Removes all instances of the given [handleName] from this RawContact.
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun MutableRawContact.removeHandleName(
    contacts: Contacts,
    handleName: MutableHandleNameEntity,
    byReference: Boolean = false
) {
    contacts.customDataRegistry.removeCustomDataEntityFrom(this, byReference, handleName)
}

/**
 * Removes all handle names from this RawContact.
 */
fun MutableRawContact.removeAllHandleNames(contacts: Contacts) {
    contacts.customDataRegistry.removeAllCustomDataEntityFrom(this, HandleNameMimeType)
}

// endregion

// region NewRawContact

/**
 * Returns the sequence of [NewHandleName]s of this RawContact.
 */
fun NewRawContact.handleNames(contacts: Contacts): Sequence<NewHandleName> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<NewHandleName>(this, HandleNameMimeType)

    return customDataEntities.asSequence()
}

/**
 * Returns the list of [NewHandleName]s of this RawContact.
 */
fun NewRawContact.handleNameList(contacts: Contacts): List<NewHandleName> =
    handleNames(contacts).toList()

/**
 * Adds the given [handleName] to this RawContact.
 */
fun NewRawContact.addHandleName(contacts: Contacts, handleName: NewHandleName) {
    contacts.customDataRegistry.putCustomDataEntityInto(this, handleName)
}

/**
 * Adds a handle name (configured by [configureHandleName]) to this RawContact.
 */
fun NewRawContact.addHandleName(
    contacts: Contacts,
    configureHandleName: NewHandleName.() -> Unit
) {
    addHandleName(contacts, NewHandleName().apply(configureHandleName))
}

/**
 * Removes all instances of the given [handleName] from this RawContact.
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun NewRawContact.removeHandleName(
    contacts: Contacts,
    handleName: NewHandleName,
    byReference: Boolean = false
) {
    contacts.customDataRegistry.removeCustomDataEntityFrom(this, byReference, handleName)
}

/**
 * Removes all handle names from this RawContact.
 */
fun NewRawContact.removeAllHandleNames(contacts: Contacts) {
    contacts.customDataRegistry.removeAllCustomDataEntityFrom(this, HandleNameMimeType)
}

// endregion