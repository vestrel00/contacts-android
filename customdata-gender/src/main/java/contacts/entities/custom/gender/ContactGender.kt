package contacts.entities.custom.gender

import contacts.entities.Contact
import contacts.entities.MutableContact
import contacts.entities.RawContact
import contacts.entities.custom.CustomDataRegistry
import contacts.entities.custom.GlobalCustomDataRegistry

/**
 * Returns the sequence of [Gender]s from all [Contact.rawContacts] ordered by the [Gender.id].
 */
fun Contact.genders(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): Sequence<Gender> = rawContacts
    .asSequence()
    .mapNotNull { it.gender(customDataRegistry) }
    .sortedBy { it.id }

/**
 * Returns the list of [Gender]s from all [Contact.rawContacts] ordered by the [Gender.id].
 */
fun Contact.genderList(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): List<Gender> = genders(customDataRegistry).toList()

/**
 * Returns the sequence of [MutableGender]s from all [Contact.rawContacts] ordered by the
 * [MutableGender.id].
 */
fun MutableContact.genders(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): Sequence<MutableGender> = rawContacts
    .asSequence()
    .mapNotNull { it.gender(customDataRegistry) }
    .sortedBy { it.id }

/**
 * Returns the list of [MutableGender]s from all [Contact.rawContacts] ordered by the
 * [MutableGender.id].
 */
fun MutableContact.genderList(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): List<MutableGender> = genders(customDataRegistry).toList()

/**
 * Sets the [RawContact.gender] of the first RawContact in [MutableContact.rawContacts] sorted by
 * the RawContact id.
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableContact] object.
 */
fun MutableContact.setGender(
    gender: MutableGender?,
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
) {
    rawContacts.firstOrNull()?.setGender(gender, customDataRegistry)
}

/**
 * Sets the [RawContact.gender] (configured by [configureGender]) of the first RawContact in
 * [MutableContact.rawContacts] sorted by the RawContact id.
 *
 * This does not perform the actual insert/update to the database. You will need to perform an
 * insert/update operation on this [MutableContact] object.
 */
fun MutableContact.setGender(
    customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry,
    configureGender: MutableGender.() -> Unit
) {
    setGender(MutableGender().apply(configureGender), customDataRegistry)
}