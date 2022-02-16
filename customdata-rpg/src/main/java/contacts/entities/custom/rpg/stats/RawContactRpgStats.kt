package contacts.entities.custom.rpg.stats

import contacts.core.Contacts
import contacts.core.entities.MutableRawContact
import contacts.core.entities.NewRawContact
import contacts.core.entities.RawContact
import contacts.entities.custom.rpg.RpgMimeType

// Dev note: The functions that return a List instead of a Sequence are useful for Java consumers
// as they will not have to convert Sequences to List. Also, all are functions instead of properties
// with getters because there are some setters that have to be functions. So all are functions
// to keep uniformity for OCD purposes.

// region RawContact

/**
 * Returns the [RpgStats] of this RawContact.
 */
fun RawContact.rpgStats(contacts: Contacts): RpgStats? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<RpgStats>(this, RpgMimeType.Stats)

    // We know that there can only be one rpgStats so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

// endregion

// region MutableRawContact

/**
 * Returns the [MutableRpgStatsEntity] of this RawContact. Null if not available (e.g. does not
 * exist in the database or was not an included field in the query).
 */
fun MutableRawContact.rpgStats(contacts: Contacts): MutableRpgStatsEntity? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<MutableRpgStatsEntity>(this, RpgMimeType.Stats)

    // We know that there can only be one rpgStats so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * Sets the rpgStats of this RawContact to the given [rpgStats].
 */
fun MutableRawContact.setRpgStats(contacts: Contacts, rpgStats: MutableRpgStatsEntity?) {
    if (rpgStats != null) {
        contacts.customDataRegistry.putCustomDataEntityInto(this, rpgStats)
    } else {
        contacts.customDataRegistry.removeAllCustomDataEntityFrom(
            this,
            RpgMimeType.Stats
        )
    }
}

/**
 * Sets the rpgStats of this RawContact to a [NewRpgStats] configured by [configureRpgStats].
 */
fun MutableRawContact.setRpgStats(contacts: Contacts, configureRpgStats: NewRpgStats.() -> Unit) {
    setRpgStats(contacts, NewRpgStats().apply(configureRpgStats))
}

// endregion

// region NewRawContact

/**
 * Returns the [NewRpgStats] of this RawContact.
 */
fun NewRawContact.rpgStats(contacts: Contacts): NewRpgStats? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<NewRpgStats>(this, RpgMimeType.Stats)

    // We know that there can only be one rpgStats so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * Sets the rpgStats of this RawContact to the given [rpgStats].
 */
fun NewRawContact.setRpgStats(contacts: Contacts, rpgStats: NewRpgStats?) {
    if (rpgStats != null) {
        contacts.customDataRegistry.putCustomDataEntityInto(this, rpgStats)
    } else {
        contacts.customDataRegistry.removeAllCustomDataEntityFrom(
            this,
            RpgMimeType.Stats
        )
    }
}

/**
 * Sets the rpgStats of this RawContact to a [NewRpgStats] configured by [configureRpgStats].
 */
fun NewRawContact.setRpgStats(contacts: Contacts, configureRpgStats: NewRpgStats.() -> Unit) {
    setRpgStats(contacts, NewRpgStats().apply(configureRpgStats))
}

// endregion