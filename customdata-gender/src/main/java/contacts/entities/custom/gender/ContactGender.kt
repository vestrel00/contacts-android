package contacts.entities.custom.gender

import contacts.core.Contacts
import contacts.core.entities.Contact
import contacts.core.entities.MutableContact
import contacts.core.entities.RawContact
import contacts.core.util.sortedById

// Dev note: The functions that return a List instead of a Sequence are useful for Java consumers
// as they will not have to convert Sequences to List. Also, all are functions instead of properties
// with getters because there are some setters that have to be functions. So all are functions
// to keep uniformity for OCD purposes.

// region Contact

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

// endregion

// region MutableContact

/**
 * Returns the sequence of [MutableGenderEntity]s from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.genders(contacts: Contacts): Sequence<MutableGenderEntity> = rawContacts
    .asSequence()
    .mapNotNull { it.gender(contacts) }
    .sortedById()

/**
 * Returns the list of [MutableGender]s from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.genderList(contacts: Contacts): List<MutableGenderEntity> =
    genders(contacts).toList()

/**
 * Sets the [RawContact.gender] of the first RawContact in [MutableContact.rawContacts] sorted by
 * the RawContact id.
 */
fun MutableContact.setGender(contacts: Contacts, gender: MutableGenderEntity?) {
    rawContacts.firstOrNull()?.setGender(contacts, gender)
}

/**
 * Sets the [RawContact.gender] (configured by [configureGender]) of the first RawContact in
 * [MutableContact.rawContacts] sorted by the RawContact id.
 */
fun MutableContact.setGender(contacts: Contacts, configureGender: NewGender.() -> Unit) {
    setGender(contacts, NewGender().apply(configureGender))
}

// endregion