package contacts.entities.custom.gender

import contacts.entities.MutableRawContact
import contacts.entities.RawContact
import contacts.entities.custom.CustomDataRegistry
import contacts.entities.custom.GlobalCustomDataRegistry

/**
 * TODO documentation
 */
fun RawContact.getGender(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): Gender? {
    val customDataEntities =
        customDataRegistry.customDataEntitiesFor<MutableGender>(this, GenderEntryId)

    // We know that there can only be one gender so we only look to at the first element.
    return customDataEntities.firstOrNull()?.toGender()
}

/**
 * TODO documentation
 */
fun MutableRawContact.getGender(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): MutableGender? {
    val customDataEntities =
        customDataRegistry.customDataEntitiesFor<MutableGender>(this, GenderEntryId)

    // We know that there can only be one gender so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * TODO documentation (note the nullable gender)
 */
fun MutableRawContact.setGender(
    gender: MutableGender?,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
) {
    if (gender != null) {
        customDataRegistry.putCustomDataEntityInto(this, gender, GenderEntryId)
    } else {
        customDataRegistry.removeAllCustomDataEntityFrom(this, GenderEntryId)
    }
}