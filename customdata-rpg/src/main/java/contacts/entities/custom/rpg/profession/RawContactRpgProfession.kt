package contacts.entities.custom.rpg.profession

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
 * Returns the [RpgProfession] of this RawContact.
 */
fun RawContact.rpgProfession(contacts: Contacts): RpgProfession? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<RpgProfession>(this, RpgMimeType.Profession)

    // We know that there can only be one rpgProfession so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

// endregion

// region MutableRawContact

/**
 * Returns the [MutableRpgProfessionEntity] of this RawContact. Null if not available (e.g. does
 * not exist in the database or was not an included field in the query).
 */
fun MutableRawContact.rpgProfession(contacts: Contacts): MutableRpgProfessionEntity? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<MutableRpgProfessionEntity>(this, RpgMimeType.Profession)

    // We know that there can only be one rpgProfession so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * Sets the rpgProfession of this RawContact to the given [rpgProfession].
 */
fun MutableRawContact.setRpgProfession(
    contacts: Contacts,
    rpgProfession: MutableRpgProfessionEntity?
) {
    if (rpgProfession != null) {
        contacts.customDataRegistry.putCustomDataEntityInto(this, rpgProfession)
    } else {
        contacts.customDataRegistry.removeAllCustomDataEntityFrom(
            this,
            RpgMimeType.Profession
        )
    }
}

/**
 * Sets the rpgProfession of this RawContact to a [NewRpgProfession] configured by
 * [configureRpgProfession].
 */
fun MutableRawContact.setRpgProfession(
    contacts: Contacts,
    configureRpgProfession: NewRpgProfession.() -> Unit
) {
    setRpgProfession(contacts, NewRpgProfession().apply(configureRpgProfession))
}

// endregion

// region NewRawContact

/**
 * Returns the [NewRpgProfession] of this RawContact.
 */
fun NewRawContact.rpgProfession(contacts: Contacts): NewRpgProfession? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<NewRpgProfession>(this, RpgMimeType.Profession)

    // We know that there can only be one rpgProfession so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * Sets the rpgProfession of this RawContact to the given [rpgProfession].
 */
fun NewRawContact.setRpgProfession(contacts: Contacts, rpgProfession: NewRpgProfession?) {
    if (rpgProfession != null) {
        contacts.customDataRegistry.putCustomDataEntityInto(this, rpgProfession)
    } else {
        contacts.customDataRegistry.removeAllCustomDataEntityFrom(
            this,
            RpgMimeType.Profession
        )
    }
}

/**
 * Sets the rpgProfession of this RawContact to a [NewRpgProfession] configured by
 * [configureRpgProfession].
 */
fun NewRawContact.setRpgProfession(
    contacts: Contacts,
    configureRpgProfession: NewRpgProfession.() -> Unit
) {
    setRpgProfession(contacts, NewRpgProfession().apply(configureRpgProfession))
}

// endregion