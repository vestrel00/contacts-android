package contacts.entities.custom.googlecontacts.fileas

import contacts.core.Contacts
import contacts.core.entities.MutableRawContact
import contacts.core.entities.NewRawContact
import contacts.core.entities.RawContact
import contacts.entities.custom.googlecontacts.GoogleContactsMimeType

// Dev note: The functions that return a List instead of a Sequence are useful for Java consumers
// as they will not have to convert Sequences to List. Also, all are functions instead of properties
// with getters because there are some setters that have to be functions. So all are functions
// to keep uniformity for OCD purposes.

// region RawContact

/**
 * Returns the [FileAs] of this RawContact.
 */
fun RawContact.fileAs(contacts: Contacts): FileAs? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<FileAs>(this, GoogleContactsMimeType.FileAs)

    // We know that there can only be one fileAs so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

// endregion

// region MutableRawContact

/**
 * Returns the [MutableFileAsEntity] of this RawContact. Null if not available (e.g. does not exist
 * in the database or was not an included field in the query).
 */
fun MutableRawContact.fileAs(contacts: Contacts): MutableFileAsEntity? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<MutableFileAsEntity>(this, GoogleContactsMimeType.FileAs)

    // We know that there can only be one fileAs so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * Sets the fileAs of this RawContact to the given [fileAs].
 */
fun MutableRawContact.setFileAs(contacts: Contacts, fileAs: MutableFileAsEntity?) {
    if (fileAs != null) {
        contacts.customDataRegistry.putCustomDataEntityInto(this, fileAs)
    } else {
        contacts.customDataRegistry.removeAllCustomDataEntityFrom(
            this,
            GoogleContactsMimeType.FileAs
        )
    }
}

/**
 * Sets the fileAs of this RawContact to a [NewFileAs] configured by [configureFileAs].
 */
fun MutableRawContact.setFileAs(contacts: Contacts, configureFileAs: NewFileAs.() -> Unit) {
    setFileAs(contacts, NewFileAs().apply(configureFileAs))
}

// endregion

// region NewRawContact

/**
 * Returns the [NewFileAs] of this RawContact.
 */
fun NewRawContact.fileAs(contacts: Contacts): NewFileAs? {
    val customDataEntities = contacts.customDataRegistry
        .customDataEntitiesFor<NewFileAs>(this, GoogleContactsMimeType.FileAs)

    // We know that there can only be one fileAs so we only look to at the first element.
    return customDataEntities.firstOrNull()
}

/**
 * Sets the fileAs of this RawContact to the given [fileAs].
 */
fun NewRawContact.setFileAs(contacts: Contacts, fileAs: NewFileAs?) {
    if (fileAs != null) {
        contacts.customDataRegistry.putCustomDataEntityInto(this, fileAs)
    } else {
        contacts.customDataRegistry.removeAllCustomDataEntityFrom(
            this,
            GoogleContactsMimeType.FileAs
        )
    }
}

/**
 * Sets the fileAs of this RawContact to a [NewFileAs] configured by [configureFileAs].
 */
fun NewRawContact.setFileAs(contacts: Contacts, configureFileAs: NewFileAs.() -> Unit) {
    setFileAs(contacts, NewFileAs().apply(configureFileAs))
}

// endregion