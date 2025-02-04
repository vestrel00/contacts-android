package contacts.entities.custom.multiplenotes

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
 * Returns the sequence of [MultipleNotes]s from all [Contact.rawContacts] ordered by id
 */
fun Contact.multipleNotes(contacts: Contacts): Sequence<MultipleNotes> = rawContacts
    .asSequence()
    .flatMap { it.multipleNotes(contacts) }
    .sortedBy { it.id }

/**
 * Returns the list of [MultipleNotes] from all [Contact.rawContacts] ordered by the
 * [MultipleNotes.id].
 */
fun Contact.multipleNotesList(contacts: Contacts): List<MultipleNotes> =
    multipleNotes(contacts).toList()

// endregion

// region MutableContact

/**
 * Returns the sequence of [MutableMultipleNotes] from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.multipleNotes(contacts: Contacts): Sequence<MutableMultipleNotesEntity> =
    rawContacts
        .asSequence()
        .flatMap { it.multipleNotes(contacts) }
        .sortedById()

/**
 * Returns the list of [MutableMultipleNotesEntity]s from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.multipleNotesList(contacts: Contacts): List<MutableMultipleNotesEntity> =
    multipleNotes(contacts).toList()

/**
 * Adds the given [multipleNotes] to the first RawContact in [MutableContact.rawContacts] sorted by
 * the RawContact id.
 */
fun MutableContact.addMultipleNotes(contacts: Contacts, multipleNotes: MutableMultipleNotesEntity) {
    rawContacts.firstOrNull()?.addMultipleNotes(contacts, multipleNotes)
}

/**
 * Adds a new multiple note (configured by [configureMultipleNotes]) to the first RawContact in
 * [MutableContact.rawContacts] sorted by the RawContact id.
 */
fun MutableContact.addMultipleNotes(
    contacts: Contacts,
    configureMultipleNotes: NewMultipleNotes.() -> Unit
) {
    addMultipleNotes(contacts, NewMultipleNotes().apply(configureMultipleNotes))
}

/**
 * Removes all instances of the given [multipleNotes] from all [MutableContact.rawContacts].
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun MutableContact.removeMultipleNotes(
    contacts: Contacts,
    multipleNotes: MutableMultipleNotesEntity,
    byReference: Boolean = false
) {
    for (rawContact in rawContacts) {
        rawContact.removeMultipleNotes(contacts, multipleNotes, byReference)
    }
}

/**
 * Removes all multiple notes from all [MutableContact.rawContacts].
 */
fun MutableContact.removeAllMultipleNotes(contacts: Contacts) {
    for (rawContact in rawContacts) {
        rawContact.removeAllMultipleNotes(contacts)
    }
}

// endregion