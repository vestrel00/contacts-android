package contacts.entities.custom.gender

import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.entities.MutableContact
import contacts.core.entities.RawContact

// Dev note: The functions that return a List instead of a Sequence are useful for Java consumers
// as they will not have to convert Sequences to List. Also, all are functions instead of properties
// with getters because there are some setters that have to be functions. So all are functions
// to keep uniformity for OCD purposes.

// Another dev note: Receiver signatures are the concrete types instead of the interface type.
// This is done so that consumers gets references to actual concrete types, which may implement
// other interfaces required by APIs in this library.

/**
 * Returns the sequence of [Gender]s from all [Contact.rawContacts] ordered by the [Gender.id].
 */
fun Contact.genders(contacts: Contacts): Sequence<Gender> = rawContacts
    .asSequence()
    .mapNotNull { it.gender(contacts) }
    .sortedBy { it.id }

/**
 * Returns the list of [Gender]s from all [Contact.rawContacts] ordered by the [Gender.id].
 */
fun Contact.genderList(contacts: Contacts): List<Gender> = genders(contacts).toList()

/**
 * Returns the sequence of [MutableGender]s from all [Contact.rawContacts] ordered by the
 * [MutableGender.id].
 */
fun MutableContact.genders(contacts: Contacts): Sequence<MutableGender> = rawContacts
    .asSequence()
    .mapNotNull { it.gender(contacts) }
    .sortedBy { it.id }

/**
 * Returns the list of [MutableGender]s from all [Contact.rawContacts] ordered by the
 * [MutableGender.id].
 */
fun MutableContact.genderList(contacts: Contacts): List<MutableGender> = genders(contacts).toList()

/**
 * Sets the [RawContact.gender] of the first RawContact in [MutableContact.rawContacts] sorted by
 * the RawContact id.
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableContact] object.
 */
fun MutableContact.setGender(contacts: Contacts, gender: MutableGender?) {
    rawContacts.firstOrNull()?.setGender(contacts, gender)
}

/**
 * Sets the [RawContact.gender] (configured by [configureGender]) of the first RawContact in
 * [MutableContact.rawContacts] sorted by the RawContact id.
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableContact] object.
 */
fun MutableContact.setGender(contacts: Contacts, configureGender: MutableGender.() -> Unit) {
    setGender(contacts, MutableGender().apply(configureGender))
}