package contacts.entities.custom.googlecontacts.fileas

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
 * Returns the sequence of [FileAs]s from all [Contact.rawContacts] ordered by the [FileAs.id].
 */
fun Contact.fileAs(contacts: Contacts): Sequence<FileAs> = rawContacts
    .asSequence()
    .mapNotNull { it.fileAs(contacts) }
    .sortedBy { it.id }

/**
 * Returns the list of [FileAs]s from all [Contact.rawContacts] ordered by the [FileAs.id].
 */
fun Contact.fileAsList(contacts: Contacts): List<FileAs> = fileAs(contacts).toList()

// endregion

// region MutableContact

/**
 * Returns the sequence of [MutableFileAsEntity]s from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.fileAs(contacts: Contacts): Sequence<MutableFileAsEntity> = rawContacts
    .asSequence()
    .mapNotNull { it.fileAs(contacts) }
    .sortedById()

/**
 * Returns the list of [MutableFileAs]s from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.fileAsList(contacts: Contacts): List<MutableFileAsEntity> =
    fileAs(contacts).toList()

/**
 * Sets the [RawContact.fileAs] of the first RawContact in [MutableContact.rawContacts] sorted by
 * the RawContact id.
 */
fun MutableContact.setFileAs(contacts: Contacts, fileAs: MutableFileAsEntity?) {
    rawContacts.firstOrNull()?.setFileAs(contacts, fileAs)
}

/**
 * Sets the [RawContact.fileAs] (configured by [configureFileAs]) of the first RawContact in
 * [MutableContact.rawContacts] sorted by the RawContact id.
 */
fun MutableContact.setFileAs(contacts: Contacts, configureFileAs: NewFileAs.() -> Unit) {
    setFileAs(contacts, NewFileAs().apply(configureFileAs))
}

// endregion