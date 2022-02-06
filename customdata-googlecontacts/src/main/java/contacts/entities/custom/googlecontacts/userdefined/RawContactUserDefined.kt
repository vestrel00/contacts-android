package contacts.entities.custom.googlecontacts.userdefined

import contacts.core.Contacts
import contacts.core.entities.MutableRawContact
import contacts.core.entities.NewRawContact
import contacts.core.entities.RawContact
import contacts.entities.custom.googlecontacts.GoogleContactsMimeType

// Dev note: The functions that return a List instead of a Sequence are useful for Java consumers
// as they will not have to convert Sequences to List. Also, all are functions instead of properties
// with getters because there are some setters that have to be functions. So all are functions
// to keep uniformity for OCD purposes.

// region RawContact

/**
 * Returns the sequence of [UserDefined] of this RawContact.
 */
fun RawContact.userDefineds(contacts: Contacts): Sequence<UserDefined> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<UserDefined>(this, GoogleContactsMimeType.UserDefined)

    return customDataEntities.asSequence()
}

/**
 * Returns the list of [UserDefined]s of this RawContact.
 */
fun RawContact.userDefinedList(contacts: Contacts): List<UserDefined> =
    userDefineds(contacts).toList()

// endregion

// region MutableRawContact

/**
 * Returns the sequence of [MutableUserDefinedEntity]s of this RawContact.
 */
fun MutableRawContact.userDefineds(contacts: Contacts): Sequence<MutableUserDefinedEntity> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<MutableUserDefinedEntity>(this, GoogleContactsMimeType.UserDefined)

    return customDataEntities.asSequence()
}

/**
 * Returns the list of [MutableUserDefinedEntity]s of this RawContact.
 */
fun MutableRawContact.userDefinedList(contacts: Contacts): List<MutableUserDefinedEntity> =
    userDefineds(contacts).toList()

/**
 * Adds the given [userDefined] to this RawContact.
 */
fun MutableRawContact.addUserDefined(
    contacts: Contacts,
    userDefined: MutableUserDefinedEntity
) {
    contacts.customDataRegistry.putCustomDataEntityInto(this, userDefined)
}

/**
 * Adds a user defined field-value (configured by [configureUserDefined]) to this RawContact.
 */
fun MutableRawContact.addUserDefined(
    contacts: Contacts,
    configureUserDefined: NewUserDefined.() -> Unit
) {
    addUserDefined(contacts, NewUserDefined().apply(configureUserDefined))
}

/**
 * Removes all instances of the given [userDefined] from this RawContact.
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun MutableRawContact.removeUserDefined(
    contacts: Contacts,
    userDefined: MutableUserDefinedEntity,
    byReference: Boolean = false
) {
    contacts.customDataRegistry.removeCustomDataEntityFrom(this, byReference, userDefined)
}

/**
 * Removes all user defined field-values from this RawContact.
 */
fun MutableRawContact.removeAllUserDefineds(contacts: Contacts) {
    contacts.customDataRegistry.removeAllCustomDataEntityFrom(
        this,
        GoogleContactsMimeType.UserDefined
    )
}

// endregion

// region NewRawContact

/**
 * Returns the sequence of [NewUserDefined]s of this RawContact.
 */
fun NewRawContact.userDefineds(contacts: Contacts): Sequence<NewUserDefined> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<NewUserDefined>(this, GoogleContactsMimeType.UserDefined)

    return customDataEntities.asSequence()
}

/**
 * Returns the list of [NewUserDefined]s of this RawContact.
 */
fun NewRawContact.userDefinedList(contacts: Contacts): List<NewUserDefined> =
    userDefineds(contacts).toList()

/**
 * Adds the given [userDefined] to this RawContact.
 */
fun NewRawContact.addUserDefined(contacts: Contacts, userDefined: NewUserDefined) {
    contacts.customDataRegistry.putCustomDataEntityInto(this, userDefined)
}

/**
 * Adds a user defined field-value (configured by [configureUserDefined]) to this RawContact.
 */
fun NewRawContact.addUserDefined(
    contacts: Contacts,
    configureUserDefined: NewUserDefined.() -> Unit
) {
    addUserDefined(contacts, NewUserDefined().apply(configureUserDefined))
}

/**
 * Removes all instances of the given [userDefined] from this RawContact.
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun NewRawContact.removeUserDefined(
    contacts: Contacts,
    userDefined: NewUserDefined,
    byReference: Boolean = false
) {
    contacts.customDataRegistry.removeCustomDataEntityFrom(this, byReference, userDefined)
}

/**
 * Removes all user defined field-values from this RawContact.
 */
fun NewRawContact.removeAllUserDefineds(contacts: Contacts) {
    contacts.customDataRegistry.removeAllCustomDataEntityFrom(
        this,
        GoogleContactsMimeType.UserDefined
    )
}

// endregion