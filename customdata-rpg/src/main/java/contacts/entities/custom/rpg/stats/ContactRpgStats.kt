package contacts.entities.custom.rpg.stats

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
 * Returns the sequence of [RpgStats]s from all [Contact.rawContacts] ordered by the [RpgStats.id].
 */
fun Contact.rpgStats(contacts: Contacts): Sequence<RpgStats> = rawContacts
    .asSequence()
    .mapNotNull { it.rpgStats(contacts) }
    .sortedBy { it.id }

/**
 * Returns the list of [RpgStats]s from all [Contact.rawContacts] ordered by the [RpgStats.id].
 */
fun Contact.rpgStatsList(contacts: Contacts): List<RpgStats> = rpgStats(contacts).toList()

// endregion

// region MutableContact

/**
 * Returns the sequence of [MutableRpgStatsEntity]s from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.rpgStats(contacts: Contacts): Sequence<MutableRpgStatsEntity> = rawContacts
    .asSequence()
    .mapNotNull { it.rpgStats(contacts) }
    .sortedById()

/**
 * Returns the list of [MutableRpgStats]s from all [Contact.rawContacts] ordered by id.
 */
fun MutableContact.rpgStatsList(contacts: Contacts): List<MutableRpgStatsEntity> =
    rpgStats(contacts).toList()

/**
 * Sets the [RawContact.rpgStats] of the first RawContact in [MutableContact.rawContacts] sorted by
 * the RawContact id.
 */
fun MutableContact.setRpgStats(contacts: Contacts, rpgStats: MutableRpgStatsEntity?) {
    rawContacts.firstOrNull()?.setRpgStats(contacts, rpgStats)
}

/**
 * Sets the [RawContact.rpgStats] (configured by [configureRpgStats]) of the first RawContact in
 * [MutableContact.rawContacts] sorted by the RawContact id.
 */
fun MutableContact.setRpgStats(contacts: Contacts, configureRpgStats: NewRpgStats.() -> Unit) {
    setRpgStats(contacts, NewRpgStats().apply(configureRpgStats))
}

// endregion