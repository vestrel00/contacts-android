package contacts.entities.custom.multiplenotes

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
 * Returns the sequence of [MultipleNotes] of this RawContact.
 */
fun RawContact.multipleNotes(contacts: Contacts): Sequence<MultipleNotes> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<MultipleNotes>(this, MultipleNotesMimeType)

    return customDataEntities.asSequence()
}

/**
 * Returns the list of [MultipleNotes] of this RawContact.
 */
fun RawContact.multipleNotesList(contacts: Contacts): List<MultipleNotes> =
    multipleNotes(contacts).toList()

// endregion

// region MutableRawContact

/**
 * Returns the sequence of [MutableMultipleNotesEntity]s of this RawContact.
 */
fun MutableRawContact.multipleNotes(contacts: Contacts): Sequence<MutableMultipleNotesEntity> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<MutableMultipleNotesEntity>(this, MultipleNotesMimeType)

    return customDataEntities.asSequence()
}

/**
 * Returns the list of [MutableMultipleNotesEntity]s of this RawContact.
 */
fun MutableRawContact.multipleNotesList(contacts: Contacts): List<MutableMultipleNotesEntity> =
    multipleNotes(contacts).toList()

/**
 * Adds the given [multipleNotes] to this RawContact.
 */
fun MutableRawContact.addMultipleNotes(
    contacts: Contacts,
    multipleNotes: MutableMultipleNotesEntity
) {
    contacts.customDataRegistry.putCustomDataEntityInto(this, multipleNotes)
}

/**
 * Adds a multiple notes (configured by [configureMultipleNotes]) to this RawContact.
 */
fun MutableRawContact.addMultipleNotes(
    contacts: Contacts,
    configureMultipleNotes: NewMultipleNotes.() -> Unit
) {
    addMultipleNotes(contacts, NewMultipleNotes().apply(configureMultipleNotes))
}

/**
 * Removes all instances of the given [multipleNotes] from this RawContact.
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun MutableRawContact.removeMultipleNotes(
    contacts: Contacts,
    multipleNotes: MutableMultipleNotesEntity,
    byReference: Boolean = false
) {
    contacts.customDataRegistry.removeCustomDataEntityFrom(this, byReference, multipleNotes)
}

/**
 * Removes all multiple notes from this RawContact.
 */
fun MutableRawContact.removeAllMultipleNotes(contacts: Contacts) {
    contacts.customDataRegistry.removeAllCustomDataEntityFrom(this, MultipleNotesMimeType)
}

// endregion

// region NewRawContact

/**
 * Returns the sequence of [NewMultipleNotes] of this RawContact.
 */
fun NewRawContact.multipleNotes(contacts: Contacts): Sequence<NewMultipleNotes> {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<NewMultipleNotes>(this, MultipleNotesMimeType)

    return customDataEntities.asSequence()
}

/**
 * Returns the list of [NewMultipleNotes] of this RawContact.
 */
fun NewRawContact.multipleNotesList(contacts: Contacts): List<NewMultipleNotes> =
    multipleNotes(contacts).toList()

/**
 * Adds the given [multipleNotes] to this RawContact.
 */
fun NewRawContact.addMultipleNotes(contacts: Contacts, multipleNotes: NewMultipleNotes) {
    contacts.customDataRegistry.putCustomDataEntityInto(this, multipleNotes)
}

/**
 * Adds a multiple notes (configured by [configureMultipleNotes]) to this RawContact.
 */
fun NewRawContact.addMultipleNotes(
    contacts: Contacts,
    configureMultipleNotes: NewMultipleNotes.() -> Unit
) {
    addMultipleNotes(contacts, NewMultipleNotes().apply(configureMultipleNotes))
}

/**
 * Removes all instances of the given [multipleNotes] from this RawContact.
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
fun NewRawContact.removeMultipleNotes(
    contacts: Contacts,
    multipleNotes: NewMultipleNotes,
    byReference: Boolean = false
) {
    contacts.customDataRegistry.removeCustomDataEntityFrom(this, byReference, multipleNotes)
}

/**
 * Removes all multiple notes from this RawContact.
 */
fun NewRawContact.removeAllMultipleNotes(contacts: Contacts) {
    contacts.customDataRegistry.removeAllCustomDataEntityFrom(this, MultipleNotesMimeType)
}

// endregion