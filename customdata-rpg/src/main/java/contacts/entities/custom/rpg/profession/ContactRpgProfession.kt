package contacts.entities.custom.rpg.profession

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
 * Returns the sequence of [RpgProfession]s from all [Contact.rawContacts] ordered by the
 * [RpgProfession.id].
 */
fun Contact.rpgProfessions(contacts: Contacts): Sequence<RpgProfession> = rawContacts
    .asSequence()
    .mapNotNull { it.rpgProfession(contacts) }
    .sortedBy { it.id }

/**
 * Returns the list of [RpgProfession]s from all [Contact.rawContacts] ordered by the
 * [RpgProfession.id].
 */
fun Contact.rpgProfessionList(contacts: Contacts): List<RpgProfession> =
    rpgProfessions(contacts).toList()

// endregion

// region MutableContact

/**
 * Returns the sequence of [MutableRpgProfessionEntity]s from all [Contact.rawContacts] ordered by
 * id.
 */
fun MutableContact.rpgProfessions(contacts: Contacts): Sequence<MutableRpgProfessionEntity> =
    rawContacts
        .asSequence()
        .mapNotNull { it.rpgProfession(contacts) }
        .sortedById()

/**
 * Returns the list of [MutableRpgProfession]s from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.rpgProfessionList(contacts: Contacts): List<MutableRpgProfessionEntity> =
    rpgProfessions(contacts).toList()

/**
 * Sets the [RawContact.rpgProfession] of the first RawContact in [MutableContact.rawContacts]
 * sorted by the RawContact id.
 */
fun MutableContact.setRpgProfession(
    contacts: Contacts,
    rpgProfession: MutableRpgProfessionEntity?
) {
    rawContacts.firstOrNull()?.setRpgProfession(contacts, rpgProfession)
}

/**
 * Sets the [RawContact.rpgProfession] (configured by [configureRpgProfession]) of the first
 * RawContact in [MutableContact.rawContacts] sorted by the RawContact id.
 */
fun MutableContact.setRpgProfession(
    contacts: Contacts,
    configureRpgProfession: NewRpgProfession.() -> Unit
) {
    setRpgProfession(contacts, NewRpgProfession().apply(configureRpgProfession))
}

// endregion