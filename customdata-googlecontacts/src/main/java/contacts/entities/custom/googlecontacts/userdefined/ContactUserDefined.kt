package contacts.entities.custom.googlecontacts.userdefined

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
 * Returns the sequence of [UserDefined]s from all [Contact.rawContacts] ordered by id
 */
fun Contact.userDefined(contacts: Contacts): Sequence<UserDefined> = rawContacts
    .asSequence()
    .flatMap { it.userDefined(contacts) }
    .sortedBy { it.id }

/**
 * Returns the list of [UserDefined]s from all [Contact.rawContacts] ordered by the [UserDefined.id].
 */
fun Contact.userDefinedList(contacts: Contacts): List<UserDefined> = userDefined(contacts).toList()

// endregion

// region MutableContact

/**
 * Returns the sequence of [MutableUserDefined]s from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.userDefined(contacts: Contacts): Sequence<MutableUserDefinedEntity> = rawContacts
    .asSequence()
    .flatMap { it.userDefined(contacts) }
    .sortedById()

/**
 * Returns the list of [MutableUserDefinedEntity]s from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.userDefinedList(contacts: Contacts): List<MutableUserDefinedEntity> =
    userDefined(contacts).toList()

/**
 * Adds the given [userDefined] to the first RawContact in [MutableContact.rawContacts] sorted by
 * the RawContact id.
 */
fun MutableContact.addUserDefined(contacts: Contacts, userDefined: MutableUserDefinedEntity) {
    rawContacts.firstOrNull()?.addUserDefined(contacts, userDefined)
}

/**
 * Adds a new user defined field-values (configured by [configureUserDefined]) to the first
 * RawContact in [MutableContact.rawContacts] sorted by the RawContact id.
 */
fun MutableContact.addUserDefined(
    contacts: Contacts,
    configureUserDefined: NewUserDefined.() -> Unit
) {
    addUserDefined(contacts, NewUserDefined().apply(configureUserDefined))
}

/**
 * Removes all instances of the given [userDefined] from all [MutableContact.rawContacts].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun MutableContact.removeUserDefined(
    contacts: Contacts,
    userDefined: MutableUserDefinedEntity,
    byReference: Boolean = false
) {
    for (rawContact in rawContacts) {
        rawContact.removeUserDefined(contacts, userDefined, byReference)
    }
}

/**
 * Removes all user defined field-values from all [MutableContact.rawContacts].
 */
fun MutableContact.removeAllUserDefined(contacts: Contacts) {
    for (rawContact in rawContacts) {
        rawContact.removeAllUserDefined(contacts)
    }
}

// endregion