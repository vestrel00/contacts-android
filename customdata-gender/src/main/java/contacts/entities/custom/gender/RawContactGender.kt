package contacts.entities.custom.gender

import contacts.core.Contacts
import contacts.core.entities.MutableRawContact
import contacts.core.entities.RawContact

/**
 * Returns the [Gender] of this RawContact. Null if not available (e.g. does not exist in the
 * database or was not an included field in the query).
 */
fun RawContact.gender(contacts: Contacts): Gender? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<MutableGender>(this, GenderMimeType)

    // We know that there can only be one gender so we only look to at the first element.
    return customDataEntities.firstOrNull()?.toGender()
}

/**
 * Returns the [MutableGender] of this RawContact. Null if not available (e.g. does not exist in
 * the database or was not an included field in the query).
 */
fun MutableRawContact.gender(contacts: Contacts): MutableGender? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<MutableGender>(this, GenderMimeType)

    // We know that there can only be one gender so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * Sets the gender of this RawContact to the given [gender].
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableRawContact] object.
 */
fun MutableRawContact.setGender(contacts: Contacts, gender: MutableGender?) {
    if (gender != null) {
        contacts.customDataRegistry.putCustomDataEntityInto(this, gender)
    } else {
        contacts.customDataRegistry.removeAllCustomDataEntityFrom(this, GenderMimeType)
    }
}

/**
 * Sets the gender of this RawContact to a new [MutableGender] configured by [configureGender].
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableRawContact] object.
 */
fun MutableRawContact.setGender(contacts: Contacts, configureGender: MutableGender.() -> Unit) {
    setGender(contacts, MutableGender().apply(configureGender))
}