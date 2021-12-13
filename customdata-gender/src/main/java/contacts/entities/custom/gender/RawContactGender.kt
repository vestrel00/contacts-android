package contacts.entities.custom.gender

import contacts.core.Contacts
import contacts.core.entities.MutableRawContact
import contacts.core.entities.NewRawContact
import contacts.core.entities.RawContact

// Dev note: The functions that return a List instead of a Sequence are useful for Java consumers
// as they will not have to convert Sequences to List. Also, all are functions instead of properties
// with getters because there are some setters that have to be functions. So all are functions
// to keep uniformity for OCD purposes.

// region RawContact

/**
 * Returns the [Gender] of this RawContact.
 */
fun RawContact.gender(contacts: Contacts): Gender? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<Gender>(this, GenderMimeType)

    // We know that there can only be one gender so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

// endregion

// region MutableRawContact

/**
 * Returns the [MutableGenderEntity] of this RawContact. Null if not available (e.g. does not exist
 * in the database or was not an included field in the query).
 */
fun MutableRawContact.gender(contacts: Contacts): MutableGenderEntity? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<MutableGenderEntity>(this, GenderMimeType)

    // We know that there can only be one gender so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * Sets the gender of this RawContact to the given [gender].
 */
fun MutableRawContact.setGender(contacts: Contacts, gender: MutableGenderEntity?) {
    if (gender != null) {
        contacts.customDataRegistry.putCustomDataEntityInto(this, gender)
    } else {
        contacts.customDataRegistry.removeAllCustomDataEntityFrom(this, GenderMimeType)
    }
}

/**
 * Sets the gender of this RawContact to a [NewGender] configured by [configureGender].
 */
fun MutableRawContact.setGender(contacts: Contacts, configureGender: NewGender.() -> Unit) {
    setGender(contacts, NewGender().apply(configureGender))
}

// endregion

// region NewRawContact

/**
 * Returns the [NewGender] of this RawContact.
 */
fun NewRawContact.gender(contacts: Contacts): NewGender? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<NewGender>(this, GenderMimeType)

    // We know that there can only be one gender so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * Sets the gender of this RawContact to the given [gender].
 */
fun NewRawContact.setGender(contacts: Contacts, gender: NewGender?) {
    if (gender != null) {
        contacts.customDataRegistry.putCustomDataEntityInto(this, gender)
    } else {
        contacts.customDataRegistry.removeAllCustomDataEntityFrom(this, GenderMimeType)
    }
}

/**
 * Sets the gender of this RawContact to a [NewGender] configured by [configureGender].
 */
fun NewRawContact.setGender(contacts: Contacts, configureGender: NewGender.() -> Unit) {
    setGender(contacts, NewGender().apply(configureGender))
}

// endregion