package contacts.entities.custom.gender

import contacts.entities.MutableRawContact
import contacts.entities.RawContact
import contacts.entities.custom.CustomDataRegistry
import contacts.entities.custom.GlobalCustomDataRegistry

/**
 * Returns the [Gender] of this RawContact. Null if not available (e.g. does not exist in the
 * database or was not an included field in the query).
 */
fun RawContact.gender(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): Gender? {
    val customDataEntities =
        customDataRegistry.customDataEntitiesFor<MutableGender>(this, GenderMimeType)

    // We know that there can only be one gender so we only look to at the first element.
    return customDataEntities.firstOrNull()?.toGender()
}

/**
 * Returns the [MutableGender] of this RawContact. Null if not available (e.g. does not exist in
 * the database or was not an included field in the query).
 */
fun MutableRawContact.gender(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): MutableGender? {
    val customDataEntities =
        customDataRegistry.customDataEntitiesFor<MutableGender>(this, GenderMimeType)

    // We know that there can only be one gender so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * Sets the gender of this RawContact to the given [gender].
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableRawContact] object.
 */
fun MutableRawContact.setGender(
    gender: MutableGender?,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
) {
    if (gender != null) {
        customDataRegistry.putCustomDataEntityInto(this, gender)
    } else {
        customDataRegistry.removeAllCustomDataEntityFrom(this, GenderMimeType)
    }
}

/**
 * Sets the gender of this RawContact to a new [MutableGender] configured by [configureGender].
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableRawContact] object.
 */
fun MutableRawContact.setGender(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    configureGender: MutableGender.() -> Unit
) {
    setGender(MutableGender().apply(configureGender), customDataRegistry)
}